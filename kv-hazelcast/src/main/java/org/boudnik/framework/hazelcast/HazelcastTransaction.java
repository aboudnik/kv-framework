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
import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class HazelcastTransaction extends Context {

    private TransactionContext hazelcastTransactionContext;
    private final CachingProvider cachingProvider;
    private final CacheConfig config;
    private final HazelcastInstance hc;

    public HazelcastTransaction(HazelcastInstance hc) {
        this(hc, new CacheConfig());
    }

    public HazelcastTransaction(HazelcastInstance hc, CacheConfig config) {
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
    protected OBJ<Object> getMementoValue(Map.Entry<Object, Object> memento) {
        return (OBJ<Object>) memento.getValue();
    }

    @SuppressWarnings("unchecked")
    public <K, V extends OBJ> V get(Class<V> clazz, K identity) {
        Map<Object, OBJ> map = getMap(clazz);
        V obj = (V) map.get(identity);
        if (obj == null) {
            Object object = cache(clazz).get(identity);
            if (object == null)
                return null;
            try {
                V v = (V) object;
                v.setKey(identity);
                map.put(identity, v);
                mementos.put(identity, Beans.clone(meta, v));
                return v;
            } catch (IntrospectionException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                throw new RuntimeException(e);
            }
        } else
            return obj;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <K, V, T extends Cache<K, V>> T cache(Class<? extends OBJ> clazz) {
        CacheManager cacheManager = cachingProvider.getCacheManager();

        Cache cache = ((cache = cacheManager.getCache(clazz.getName())) == null)
                ? cacheManager.createCache(clazz.getName(), config)
                : cache;

        return (T) cache;
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
}
