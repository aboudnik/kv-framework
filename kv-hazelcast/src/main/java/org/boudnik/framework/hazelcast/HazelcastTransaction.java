package org.boudnik.framework.hazelcast;

import com.hazelcast.cache.impl.HazelcastServerCachingProvider;
import com.hazelcast.config.CacheConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.transaction.TransactionContext;
import org.boudnik.framework.Context;
import org.boudnik.framework.OBJ;
import org.boudnik.framework.util.Beans;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.spi.CachingProvider;

public class HazelcastTransaction extends Context {

    private TransactionContext hazelcastTransactionContext;
    private final CachingProvider cachingProvider;
    private final CacheConfig config;
    private final HazelcastInstance hc;

    public HazelcastTransaction(HazelcastInstance hc) {
        this(hc, new CacheConfig<>());
    }

    public <K, V extends OBJ<K>> HazelcastTransaction(HazelcastInstance hc, CacheConfig<K, V> config) {
        this.hc = hc;
        cachingProvider = HazelcastServerCachingProvider.createCachingProvider(hc);
        this.config = config;
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
    protected void engineSpecificClearAction() {
        hazelcastTransactionContext = null;
    }

    @Override
    protected boolean isTransactionExist() {
        return hazelcastTransactionContext != null;
    }

    @Override
    protected <K, V extends OBJ<K>> V toObject(Object external, K identity) throws Exception {
        //noinspection unchecked
        V v = Beans.clone(meta, (V) external);
        v.setKey(identity);
        return v;
    }

    @Override
    protected <K> Object getExternal(Class<? extends OBJ> clazz, K identity) {
        return cache(clazz).get(identity);
    }

    @Override
    protected <K, V extends OBJ<K>> Cache<K, V> cache(Class<? extends OBJ> clazz) {
        CacheManager cacheManager = cachingProvider.getCacheManager();
        Cache<K, V> cache;
        if ((cache = cacheManager.getCache(clazz.getName())) == null)
            cache = cacheManager.createCache(clazz.getName(), getConfig());
        return cache;
    }

    public HazelcastTransaction tx() {
        hazelcastTransactionContext = hc.newTransactionContext();
        hazelcastTransactionContext.beginTransaction();
        return this;
    }

    @Override
    protected void startTransactionIfNotStarted() {
        if (!isTransactionExist()) {
            hazelcastTransactionContext = hc.newTransactionContext();
            hazelcastTransactionContext.beginTransaction();
        }
    }

    private  <K, V extends OBJ<K>> CacheConfig<K, V> getConfig() {
        //noinspection unchecked
        return config;
    }
}
