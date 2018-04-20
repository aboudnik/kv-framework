package org.boudnik.framework.ignite;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.binary.BinaryObject;
import org.boudnik.framework.OBJ;
import org.boudnik.framework.Context;

import javax.cache.Cache;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Alexandre_Boudnik
 * @since 11/15/2017
 */
public class IgniteTransaction extends Context {

    private final Map<OBJ, BinaryObject> mementos = new HashMap<>();
    private final Ignite ignite;

    public IgniteTransaction(Ignite ignite) {
        this.ignite = ignite;
    }

    protected void engineSpecificCommitAction() {
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

    @SuppressWarnings("unchecked")
    public <K, V extends OBJ> V get(Class<V> clazz, K identity) {
        Map<Object, OBJ> map = getMap(clazz);
        V obj = (V) map.get(identity);
        if (obj == null) {
            if (map.containsKey(identity))
                return null;
            else {
                BinaryObject binaryObject = igniteCache(clazz).<K, BinaryObject>withKeepBinary().get(identity);
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

    private IgniteCache<Object, BinaryObject> igniteCache(Class<? extends OBJ> clazz) {
        return ignite.cache(clazz.getName());
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T extends Cache> T cache(Class<? extends OBJ> clazz) {
        return (T) igniteCache(clazz);
    }

    public IgniteTransaction tx() {
        ignite.transactions().txStart();
        return this;
    }

    protected void startTransactionIfNotStarted() {
        if (!isTransactionExist())
            ignite.transactions().txStart();
    }
}
