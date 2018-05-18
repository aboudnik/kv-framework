package org.boudnik.framework.hazelcast;

import com.hazelcast.cache.impl.HazelcastServerCachingProvider;
import com.hazelcast.config.CacheConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.transaction.TransactionContext;
import org.boudnik.framework.Context;
import org.boudnik.framework.OBJ;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.spi.CachingProvider;
import java.util.HashMap;
import java.util.Map;

public class HazelcastContext_3_9 extends Context {

    private TransactionContext hazelcastTransactionContext;
    private final CachingProvider cachingProvider;
    private final CacheConfig config;
    private final HazelcastInstance hc;
    private static Map<String, Cache> caches = new HashMap<>();


    public HazelcastContext_3_9(HazelcastInstance hc) {
        this(hc, new CacheConfig<>());
    }

    @SuppressWarnings("WeakerAccess")
    public <K, V extends OBJ<K>> HazelcastContext_3_9(HazelcastInstance hc, CacheConfig<K, V> config) {
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
    protected <K, V extends OBJ<K>> V toObject(Object external, K identity) {
        //noinspection unchecked
        V v = beans.clone((V) external);
        v.setKey(identity);
        return v;
    }

    @Override
    public HazelcastContext_3_9 withCache(Class... classes) {
        CacheManager cacheManager = cachingProvider.getCacheManager();
        for (Class clazz : classes) {
            if (caches.get(clazz.getName()) == null) {
                caches.put(clazz.getName(), cacheManager.createCache(clazz.getName(), getConfig()));
            }
        }
        return this;
    }

    @Override
    public <K, V extends OBJ<K>> Cache<K, V> cache(Class<? extends OBJ> clazz) {
        return caches.get(clazz.getName());
    }

    public HazelcastContext_3_9 tx() {
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

    private <K, V extends OBJ<K>> CacheConfig<K, V> getConfig() {
        //noinspection unchecked
        return config;
    }
}
