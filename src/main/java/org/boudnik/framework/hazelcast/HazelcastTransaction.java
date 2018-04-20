package org.boudnik.framework.hazelcast;

import com.hazelcast.cache.impl.HazelcastServerCachingProvider;
import com.hazelcast.config.CacheConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.transaction.TransactionContext;
import org.boudnik.framework.OBJ;
import org.boudnik.framework.Context;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.spi.CachingProvider;
import java.util.HashMap;
import java.util.Map;

public class HazelcastTransaction extends Context {

    private TransactionContext hazelcastTransactionContext;
    private final Map<OBJ, Object> mementos = new HashMap<>();
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
    protected boolean isTransactionExist() {
        return hazelcastTransactionContext != null;
    }

    @Override
    protected void doPut(Class<? extends OBJ> clazz, Map<Object, OBJ> map) {
        Cache<Object, Object> cache = cache(clazz);
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
                V v = (V) binaryObject;
                v.setKey(identity);
                map.put(identity, v);
                mementos.put(v, binaryObject);
                return v;
            }
        } else
            return obj;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T extends Cache> T cache(Class<? extends OBJ> clazz) {
        CacheManager cacheManager = cachingProvider.getCacheManager();

        Cache cache = ((cache = cacheManager.getCache(clazz.getName())) == null)
                ? cacheManager.createCache(clazz.getName(), config)
                : cache;

        return (T) cache;
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
        if (!isTransactionExist()) {
            hazelcastTransactionContext = hc.newTransactionContext();
            hazelcastTransactionContext.beginTransaction();
        }
    }
}
