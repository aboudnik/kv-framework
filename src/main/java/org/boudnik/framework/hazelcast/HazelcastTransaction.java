package org.boudnik.framework.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.transaction.TransactionContext;
import org.boudnik.framework.OBJ;
import org.boudnik.framework.Transaction;

import java.util.HashMap;
import java.util.Map;

public class HazelcastTransaction extends Transaction {

    private TransactionContext hazelcastTransactionContext;
    private final Map<OBJ, Object> mementos = new HashMap<>();
    private final HazelcastInstance hc;

    public HazelcastTransaction(HazelcastInstance hc) {
        this.hc = hc;
    }

    @Override
    protected void engineSpecificCommitAction() {
        hazelcastTransactionContext.commitTransaction();
    }

    @Override
    protected void engineSpecificRollbackAction() {
        if (isTransactionExist())
            hazelcastTransactionContext.rollbackTransaction();
    }

    @Override
    protected boolean isTransactionExist() {
        return hazelcastTransactionContext != null;
    }

    @Override
    protected void doRemove(Class<? extends org.boudnik.framework.OBJ> clazz, Map<Object, org.boudnik.framework.OBJ> map) {
        map.keySet().forEach(cache(clazz)::remove);
    }

    @Override
    protected void doPut(Class<? extends org.boudnik.framework.OBJ> clazz, Map<Object, org.boudnik.framework.OBJ> map) {
        Map<Object, Object> cache = cache(clazz);
        Map<Object, Object> map2Cache = new HashMap<>();
        for (Map.Entry<Object, OBJ> entry : map.entrySet()) {
            OBJ obj = entry.getValue();

            Object memento = mementos.get(obj);
            if (memento != null && !obj.equals(memento)) {
                obj.onCommit(obj, memento);
            }
            map2Cache.put(entry.getKey(), obj);
        }

        cache.putAll(map2Cache);
    }

    protected void clear() {
        hazelcastTransactionContext = null;
        super.clear();
        mementos.clear();
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
                Object binaryObject = cache(clazz).get(identity);
                if (binaryObject == null)
                    return null;
                V v = (V)binaryObject;
                v.setKey(identity);
                map.put(identity, v);
                mementos.put(v, binaryObject);
                return v;
            }
        } else
            return obj;
    }

    void revert(OBJ obj) {
//        unSave(obj);
        //todo
        cache(obj.getClass()).remove(obj.getKey());
    }

    private Map<Object, Object> cache(Class<? extends OBJ> clazz) {
        return hc.getMap(clazz.getName());
    }

    @Override
    public void close() {
        if (isTransactionExist())
            rollback();
    }

    public HazelcastTransaction tx() {
        hazelcastTransactionContext = hc.newTransactionContext();
        hazelcastTransactionContext.beginTransaction();
        return this;
    }

    @Override
    protected void startTransactionIfNotStarted() {
        if(!isTransactionExist())
        {
            hazelcastTransactionContext = hc.newTransactionContext();
            hazelcastTransactionContext.beginTransaction();
        }
    }
}
