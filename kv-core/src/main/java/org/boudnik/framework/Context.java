package org.boudnik.framework;

import org.boudnik.framework.util.Beans;

import javax.cache.Cache;
import java.beans.BeanInfo;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Alexandre_Boudnik
 * @since 11/15/2017
 */
public abstract class Context implements AutoCloseable {
    private final Map scope = new HashMap();
    private final Set<OBJ> deleted = new HashSet<>();
    private final Map<Object, Object> mementos = new HashMap<>();
    protected final Map<Class, BeanInfo> meta = new HashMap<>();

    protected abstract <K> Object getExternal(Class<? extends OBJ> clazz, K identity) throws Exception;

    protected abstract void startTransactionIfNotStarted();

    protected abstract boolean isTransactionExist();

    protected abstract void engineSpecificCommitAction();

    protected abstract void engineSpecificRollbackAction();

    protected abstract void engineSpecificClearAction();

    protected abstract <K, V extends OBJ<K>> Cache<K, V> cache(Class<? extends OBJ> clazz);

    protected abstract <K, V extends OBJ<K>> V toObject(Object external, K identity) throws Exception;

    private <K, V extends OBJ<K>> V getMementoValue(Map.Entry<K, Object> memento, Object value) throws Exception {
        return toObject(value, memento.getKey());
    }

    public final <K, V extends OBJ<K>> V get(Class<V> clazz, K identity) {
        final Map<K, V> map = getMap(clazz);
        V obj = map.get(identity);
        if (obj == null) {
            try {
                Object external = getExternal(clazz, identity);
                if (external == null)
                    return null;
                V v = toObject(external, identity);
                map.put(identity, v);
                mementos.put(identity, external);
                return v;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else
            return obj;
    }

    public <K, V extends OBJ<K>> V save(V obj) {
        return save(obj, obj.getKey());
    }

    public <K, V extends OBJ<K>> V save(V obj, K key) {
        if (key == null)
            throw new NullPointerException();
        this.<K, V>getMap(obj.getClass()).put(key, obj);
        return obj;
    }

    public <K, V extends OBJ<K>> void delete(V obj) {
        V removed = this.<K, V>getMap(obj.getClass()).remove(obj.getKey());
        if (removed != null)
            deleted.add(removed);
    }

// TODO: 04/30/2018
//    protected <K, V extends OBJ<K>> void revert(V obj) {
//        unSave(obj);
//        cache(obj.getClass()).remove(obj.getKey());
//    }

    private <K, V extends OBJ<K>> Map<K, V> getMap(Class<? extends OBJ> clazz) {
        return this.<K, V>getScope().computeIfAbsent(clazz, cls -> new HashMap<>());
    }

    public <K, V extends OBJ<K>> void rollback() {
        try {
            Map<K, Object> mementos = getMementos();
            for (Map.Entry<K, Object> memento : mementos.entrySet()) {
                V src = getMementoValue(memento, memento.getValue());
                Object dst = getMap(src.getClass()).get(memento.getKey());
                if (dst != null)
                    Beans.set(meta, src, dst);
            }
            engineSpecificRollbackAction();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            clear();
        }
    }

    @SuppressWarnings("unchecked")
    private <K, V extends OBJ<K>> Map<Class<? extends OBJ>, Map<K, V>> getScope() {
        return (Map<Class<? extends OBJ>, Map<K, V>>) scope;
    }

    @SuppressWarnings("unchecked")
    private <K> Map<K, Object> getMementos() {
        return (Map<K, Object>) mementos;
    }

    private <K, V extends OBJ<K>> void commit() {
        try {
            Map<Class<? extends OBJ>, Map<K, V>> scope = getScope();
            for (Map.Entry<Class<? extends OBJ>, Map<K, V>> byClass : scope.entrySet()) {
                Cache<K, V> cache = cache(byClass.getKey());
                cache.putAll(byClass.getValue());
            }
            for (Map.Entry<? extends Class<? extends OBJ>, Set<Object>> entry : deleted.stream().collect(Collectors.groupingBy(OBJ::getClass, Collectors.mapping(OBJ::getKey, Collectors.toSet()))).entrySet()) {
                cache(entry.getKey()).removeAll(entry.getValue());
            }
            engineSpecificCommitAction();
        } finally {
            clear();
        }
    }

    public Context transaction(OBJ obj) {
        return transaction(obj::save);
    }

    private void clear() {
        engineSpecificClearAction();
        scope.clear();
        mementos.clear();
        deleted.clear();
    }

    public Context transaction(Transactionable transactionable) {
        startTransactionIfNotStarted();
        try {
            transactionable.commit();
            commit();
        } catch (Exception e) {
            rollback();
            throw new RuntimeException(e);
        }
        return this;
    }

    <K, V extends OBJ<K>> boolean isDeleted(V reference) {
        return deleted.contains(reference);
    }

    public static Context instance() {
        return TransactionFactory.getCurrentTransaction();
    }

    @Override
    public void close() {
        if (isTransactionExist())
            rollback();
    }

    public <K, V extends OBJ<K>> V getAndClose(Class<V> clazz, K identity) {
        V value = get(clazz, identity);
        close();
        return value;
    }
}
