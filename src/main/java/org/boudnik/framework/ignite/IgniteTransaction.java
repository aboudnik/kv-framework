package org.boudnik.framework.ignite;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.binary.BinaryObject;
import org.boudnik.framework.OBJ;
import org.boudnik.framework.Transaction;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Alexandre_Boudnik
 * @since 11/15/2017
 */
public class IgniteTransaction extends Transaction {

    private final Map<OBJ, BinaryObject> mementos = new HashMap<>();
    private final Ignite ignite;

    public IgniteTransaction(Ignite ignite) {
        this.ignite = ignite;
    }

    protected void engineSpecificCommitAction(){
        ignite.transactions().tx().commit();
    }

    @Override
    protected void engineSpecificRollbackAction() {
        org.apache.ignite.transactions.Transaction tx = ignite.transactions().tx();
        if (isTransactionExist())
            tx.rollback();
    }

    @Override
    protected boolean isTransactionExist() {
        return ignite.transactions().tx() != null;
    }

    public IgniteTransaction withCache(Class... classes) {
        for (Class clazz : classes) {
            ignite.getOrCreateCache(clazz.getName());
        }
        return this;
    }

    protected void clear() {
        super.clear();
        mementos.clear();
    }

    protected void doPut(Class<? extends OBJ> clazz, Map<Object, OBJ> map) {
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

    protected void doRemove(Class<? extends OBJ> clazz, Map<Object, OBJ> map) {
        cache(clazz).removeAll(map.keySet());
    }

    protected void doRollback(@SuppressWarnings("unused") Class<? extends OBJ> clazz, @SuppressWarnings("unused") Map<Object, OBJ> map, @SuppressWarnings("unused") boolean isTombstone) {
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

    protected void revert(OBJ obj) {
//        unSave(obj);
        //todo
        cache(obj.getClass()).remove(obj.getKey());
    }

    private IgniteCache<Object, BinaryObject> cache(Class<? extends OBJ> clazz) {
        return ignite.cache(clazz.getName());
    }

    public IgniteTransaction tx() {
        ignite.transactions().txStart();
        return this;
    }

    protected void startTransactionIfNotStarted(){
        if(!isTransactionExist())
            ignite.transactions().txStart();
    }
}
