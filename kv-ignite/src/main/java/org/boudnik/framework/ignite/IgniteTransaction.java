package org.boudnik.framework.ignite;

import org.apache.ignite.Ignite;
import org.apache.ignite.binary.BinaryObject;
import org.boudnik.framework.Context;
import org.boudnik.framework.OBJ;

import javax.cache.Cache;

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
        if (isTransactionExist())
            ignite.transactions().tx().rollback();
    }

    @Override
    protected void engineSpecificClearAction() {
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

    @Override
    protected <K, V extends OBJ<K>> V toObject(Object external, K identity) {
        V v = ((BinaryObject) external).deserialize();
        v.setKey(identity);
        return v;
    }

    @Override
    protected <K> Object getExternal(Class<? extends OBJ> clazz, K identity) {
        return ignite.cache(clazz.getName()).withKeepBinary().get(identity);
    }

    @Override
    protected <K, V extends OBJ<K>> Cache<K, V> cache(Class<? extends OBJ> clazz) {
        return ignite.cache(clazz.getName());
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
