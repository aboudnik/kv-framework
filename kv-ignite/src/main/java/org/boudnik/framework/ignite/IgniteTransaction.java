package org.boudnik.framework.ignite;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.binary.BinaryObject;
import org.boudnik.framework.Context;
import org.boudnik.framework.OBJ;

import javax.cache.Cache;
import java.util.Map;

/**
 * @author Alexandre_Boudnik
 * @since 11/15/2017
 */
public class IgniteTransaction extends Context {

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
    protected void engineSpecificClearAction() {
    }

    @Override
    protected OBJ<Object> getMementoValue(Map.Entry<Object, Object> memento) {
        OBJ<Object> src = ((BinaryObject) memento.getValue()).deserialize();
        src.setKey(memento.getKey());
        return src;
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

    @SuppressWarnings("unchecked")
    public <K, V extends OBJ> V get(Class<V> clazz, K identity) {
        Map<Object, OBJ> map = getMap(clazz);
        V obj = (V) map.get(identity);
        if (obj == null) {
            BinaryObject binaryObject = igniteCache(clazz).<K, BinaryObject>withKeepBinary().get(identity);
            if (binaryObject == null)
                return null;
            V v = binaryObject.deserialize();
            v.setKey(identity);
            map.put(identity, v);
            mementos.put(identity, binaryObject);
            return v;
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
