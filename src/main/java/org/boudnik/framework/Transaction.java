package org.boudnik.framework;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.binary.BinaryObject;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Alexandre_Boudnik
 * @since 11/15/2017
 */
public class Transaction implements AutoCloseable {
    private static final ThreadLocal<Transaction> TRANSACTION_THREAD_LOCAL =
            ThreadLocal.withInitial(() -> new Transaction(Ignition.getOrStart(new IgniteConfiguration())));

    private final Map<Class<? extends OBJ>, Map<Object, OBJ>> scope = new HashMap<>();
    private final Map<OBJ, BinaryObject> mementos = new HashMap<>();
    private final Ignite ignite;

    private Transaction(Ignite ignite) {
        this.ignite = ignite;
    }

    private void commit() {
        try {
            walk(this::doCommit);
            ignite.transactions().tx().commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            clear();
        }
    }

    public static Transaction instance() {
        return TRANSACTION_THREAD_LOCAL.get();
    }

    public Transaction withCache(Class... classes) {
        for (Class clazz : classes) {
            ignite.getOrCreateCache(clazz.getName());
        }
        return this;
    }

    public void rollback() {
        try {
            walk(this::doRollback);
            org.apache.ignite.transactions.Transaction tx = ignite.transactions().tx();
            if (tx != null)
                tx.rollback();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            clear();
        }
    }

    private void clear() {
        scope.clear();
        mementos.clear();
    }

    private void doCommit(Class<? extends OBJ> clazz, Map<Object, OBJ> map, boolean isTombstone) {
        if (isTombstone) {
            doRemove(clazz, map);
        } else {
            doPut(clazz, map);
        }
    }

    private void doPut(Class<? extends OBJ> clazz, Map<Object, OBJ> map) {
        IgniteCache<Object, BinaryObject> cache = cache(clazz);
        Map<Object, BinaryObject> map2Cache = new HashMap<>();
        for (Map.Entry<Object, OBJ> entry : map.entrySet()) {
            OBJ obj = entry.getValue();
            BinaryObject current = ignite.binary().toBinary(obj);

            BinaryObject memento = mementos.get(obj);
            if (memento != null && !current.equals(memento)) {
                obj.onCommit(current, memento);
            }
            map2Cache.put(entry.getKey(), current);
        }

        cache.putAll(map2Cache);
    }

    private void doRemove(Class<? extends OBJ> clazz, Map<Object, OBJ> map) {
        cache(clazz).removeAll(map.keySet());
    }

    private void doRollback(@SuppressWarnings("unused") Class<? extends OBJ> clazz, @SuppressWarnings("unused") Map<Object, OBJ> map, boolean isTombstone) {
/*
        for (@SuppressWarnings("unused") Map.Entry<OBJ, BinaryObject> memento : mementos.entrySet()) {
            BinaryObject binary = memento.getValue();
            try {
                Map<String, PropertyDescriptor> pds = new HashMap<>();
                for (PropertyDescriptor pd : Introspector.getBeanInfo(clazz).getPropertyDescriptors())
                    pds.put(pd.getName(), pd);
                for (String field : binary.type().fieldNames()) {
                    pds.get(field).setValue(field, binary.field(field));
                }
                for (PropertyDescriptor pd : Introspector.getBeanInfo(clazz).getPropertyDescriptors())
                    pd.setValue(pd.getName(), binary.field(pd.getName()));
            } catch (IntrospectionException e) {
                e.printStackTrace();
            }
        }
*/
    }

    private void walk(Worker worker) {
        for (Map.Entry<Class<? extends OBJ>, Map<Object, OBJ>> byClass : scope.entrySet()) {

            Map<Boolean, Map<Object, OBJ>> tombstoneNotTombstoneMap = getTombstoneNotTombstoneMap(byClass);
            for(Map.Entry<Boolean, Map<Object, OBJ>> tombstoneTypePartitionEntry: tombstoneNotTombstoneMap.entrySet()){
                worker.accept(byClass.getKey(), tombstoneTypePartitionEntry.getValue(), tombstoneTypePartitionEntry.getKey());
            }
        }
    }

    @NotNull
    private Map<Boolean, Map<Object, OBJ>> getTombstoneNotTombstoneMap(Map.Entry<Class<? extends OBJ>, Map<Object, OBJ>> byClass) {
        Map<Boolean, Map<Object, OBJ>> tombstoneNotTombstoneMap = new HashMap<>();

        for(Map.Entry<Object, OBJ> entry: byClass.getValue().entrySet()){
            if(entry.getValue() == OBJ.TOMBSTONE){
                tombstoneNotTombstoneMap.computeIfAbsent(Boolean.TRUE, k -> new HashMap<>());
                tombstoneNotTombstoneMap.get(Boolean.TRUE).put(entry.getKey(), entry.getValue());
            } else {
                tombstoneNotTombstoneMap.computeIfAbsent(Boolean.FALSE, k -> new HashMap<>());
                tombstoneNotTombstoneMap.get(Boolean.FALSE).put(entry.getKey(), entry.getValue());
            }
        }
        return tombstoneNotTombstoneMap;
    }

    @NotNull
    private Map<Object, OBJ> getMap(Class<? extends OBJ> clazz) {
        return scope.computeIfAbsent(clazz, k -> new HashMap<>());
    }

    public <K, V extends OBJ> V getAndClose(Class<V> clazz, K identity) {
        V value = get(clazz, identity);
        close();
        return value;
    }

    @SuppressWarnings("unchecked")
    public <K, V extends OBJ> V get(Class<V> clazz, K identity) {
        Map<Object, OBJ> map = getMap(clazz);

        V obj = (V) map.get(identity);

        if (obj == null) {
            if (map.containsKey(identity))
                return null;
            else {
                BinaryObject binaryObject = cache(clazz).<K, BinaryObject>withKeepBinary().get(identity);
                if (binaryObject == null)
                    return null;
                V v = binaryObject.deserialize();
                v.setKey(identity);
                map.put(identity, v);
                mementos.put(v, binaryObject);
                return v;
            }
        } else
            return obj;
    }

    OBJ save(OBJ obj) {
        return save(obj, obj.getKey());
    }

    OBJ save(OBJ obj, Object key) {
        getMap(obj.getClass()).put(key, obj);
        return obj;
    }

    void delete(OBJ obj) {
        getMap(obj.getClass()).put(obj.getKey(), OBJ.TOMBSTONE);
    }

    void revert(OBJ obj) {
//        unSave(obj);
        //todo
        cache(obj.getClass()).remove(obj.getKey());
    }

    private IgniteCache<Object, BinaryObject> cache(Class<? extends OBJ> clazz) {
        return ignite.cache(clazz.getName());
    }

    @Override
    public void close() {
        if (ignite.transactions().tx() != null)
            rollback();
    }

//    <K, V extends OBJ<K>> boolean isDeleted(V reference) {
//        return OBJ.TOMBSTONE == getInScope(reference);
//    }

    static <K, V extends OBJ<K>> boolean isDeleted(V reference) {
        return OBJ.TOMBSTONE == reference;
    }

    public Transaction tx() {
        ignite.transactions().txStart();
        return this;
    }

    public Transaction txCommit(OBJ obj) {
        return txCommit(obj::save);
    }

    public Transaction txCommit(Transactionable transactionable) {
        if (ignite.transactions().tx() == null)
            ignite.transactions().txStart();
        try {
            transactionable.commit();
            commit();
        } catch (Exception e) {
            rollback();
            throw new RuntimeException(e);
        }
        return this;
    }

    @FunctionalInterface
    interface Worker {
        /**
         * Performs this operation on the given arguments.
         *
         * @param c   class
         * @param map (key -> value)
         */
//        <K, V>
        void accept(Class<? extends OBJ> c, Map<Object, OBJ> map, boolean isTombstone);

    }
}
