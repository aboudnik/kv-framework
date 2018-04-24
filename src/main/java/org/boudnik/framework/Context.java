package org.boudnik.framework;

import org.apache.ignite.binary.BinaryObject;
import org.jetbrains.annotations.NotNull;

import javax.cache.Cache;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Alexandre_Boudnik
 * @since 11/15/2017
 */
public abstract class Context implements AutoCloseable {
    private final Map<Class<? extends OBJ>, Map<Object, OBJ>> scope = new HashMap<>();
    protected final Map<OBJ, Object> mementos = new HashMap<>();

    public abstract <K, V extends OBJ> V get(Class<V> clazz, K identity);

    protected abstract void doPut(Class<? extends OBJ> clazz, Map<Object, OBJ> map);

    private void doRollback() throws IllegalAccessException {
        for (Map.Entry<OBJ, Object> memento : mementos.entrySet()) {
            Object value = getMementoValue(memento);
            OBJ key = memento.getKey();
            for (Field field : value.getClass().getDeclaredFields()) {
                Object oldValue = field.get(value);
                if (!Objects.equals(oldValue, field.get(key))) {
                    field.set(key, oldValue);
                }
            }
        }
        engineSpecificRollbackAction();
    }

    protected abstract Object getMementoValue(Map.Entry memento);

    protected abstract void startTransactionIfNotStarted();

    protected abstract boolean isTransactionExist();

    protected abstract void engineSpecificCommitAction();

    protected abstract void engineSpecificRollbackAction();

    protected abstract <T extends Cache> T cache(Class<? extends OBJ> clazz);

    public OBJ save(OBJ obj) {
        return save(obj, obj.getKey());
    }

    public OBJ save(OBJ obj, Object key) {
        if (key == null)
            throw new NullPointerException();
        getMap(obj.getClass()).put(key, obj);
        return obj;
    }

    public void delete(OBJ obj) {
        getMap(obj.getClass()).put(obj.getKey(), OBJ.TOMBSTONE);
    }

    @SuppressWarnings("unchecked")
    protected void revert(OBJ obj) {
//        unSave(obj);
        //todo
        cache(obj.getClass()).remove(obj.getKey());
    }

    @NotNull
    protected Map<Object, OBJ> getMap(Class<? extends OBJ> clazz) {
        return scope.computeIfAbsent(clazz, k -> new HashMap<>());
    }

    public void rollback() {
        try {
            doRollback();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
            clear();
        }
    }

    private void commit() {
        try {
            walk(this::doCommit);
            engineSpecificCommitAction();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            clear();
        }
    }

    private void doCommit(Class<? extends OBJ> clazz, Map<Object, OBJ> map, boolean isTombstone) {
        if (isTombstone) {
            doRemove(clazz, map);
        } else {
            doPut(clazz, map);
        }
    }

    @SuppressWarnings("unchecked")
    private void doRemove(Class<? extends OBJ> clazz, Map<Object, OBJ> map) {
        //   map.keySet().forEach(cache(clazz)::remove);
        cache(clazz).removeAll(map.keySet());
    }

    public Context transaction(OBJ obj) {
        return transaction(obj::save);
    }

    @NotNull
    private Map<Boolean, Map<Object, OBJ>> getTombstoneNotTombstoneMap(Map.Entry<Class<? extends OBJ>, Map<Object, OBJ>> byClass) {
        Map<Object, OBJ> map;
        if ((map = byClass.getValue()).isEmpty()) return Collections.emptyMap();
        Map<Boolean, Map<Object, OBJ>> tombstoneNotTombstoneMap = new HashMap<>(2);

        for (Map.Entry<Object, OBJ> entry : map.entrySet()) {
            tombstoneNotTombstoneMap
                    .computeIfAbsent(entry.getValue() == OBJ.TOMBSTONE, k -> new HashMap<>())
                    .put(entry.getKey(), entry.getValue());
        }
        return tombstoneNotTombstoneMap;
    }

    protected void clear() {
        scope.clear();
    }

    @SuppressWarnings("unchecked")
    public <T extends Context> T transaction(Transactionable transactionable) {
        startTransactionIfNotStarted();
        try {
            transactionable.commit();
            commit();
        } catch (Exception e) {
            rollback();
            throw new RuntimeException(e);
        }
        return (T) this;
    }

    static <K, V extends OBJ<K>> boolean isDeleted(V reference) {
        return OBJ.TOMBSTONE == reference;
    }

    public static <T extends Context> T instance() {
        return TransactionFactory.getCurrentTransaction();
    }

    @Override
    public void close() {
        if (isTransactionExist())
            rollback();
    }

    public <K, V extends OBJ> V getAndClose(Class<V> clazz, K identity) {
        V value = get(clazz, identity);
        close();
        return value;
    }

    private void walk(Worker worker) throws IllegalAccessException {
        for (Map.Entry<Class<? extends OBJ>, Map<Object, OBJ>> byClass : scope.entrySet()) {

            Map<Boolean, Map<Object, OBJ>> tombstoneNotTombstoneMap = getTombstoneNotTombstoneMap(byClass);
            for (Map.Entry<Boolean, Map<Object, OBJ>> tombstoneTypePartitionEntry : tombstoneNotTombstoneMap.entrySet()) {
                worker.accept(byClass.getKey(), tombstoneTypePartitionEntry.getValue(), tombstoneTypePartitionEntry.getKey());
            }
        }
    }

    @FunctionalInterface
    protected interface Worker {
        /**
         * Performs this operation on the given arguments.
         *
         * @param c           class
         * @param map         (key -> value)
         * @param isTombstone == OBJ.TOMBSTONE or NOT
         */
//        <K, V>
        void accept(Class<? extends OBJ> c, Map<Object, OBJ> map, boolean isTombstone) throws IllegalAccessException;

    }
}
