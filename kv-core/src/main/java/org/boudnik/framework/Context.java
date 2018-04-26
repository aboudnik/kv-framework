package org.boudnik.framework;

import org.boudnik.framework.util.Beans;

import javax.cache.Cache;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Alexandre_Boudnik
 * @since 11/15/2017
 */
public abstract class Context implements AutoCloseable {
    private final Map<Class<? extends OBJ>, Map<Object, OBJ>> scope = new HashMap<>();
    private final Set<OBJ> deleted = new HashSet<>();
    private Map<Class, BeanInfo> meta = new HashMap<>();
    protected final Map<Object, Object> mementos = new HashMap<>();

    public abstract <K, V extends OBJ> V get(Class<V> clazz, K identity);

    protected abstract OBJ<Object> getMementoValue(Map.Entry<Object, Object> memento);

    protected abstract void startTransactionIfNotStarted();

    protected abstract boolean isTransactionExist();

    protected abstract void engineSpecificCommitAction();

    protected abstract void engineSpecificRollbackAction();

    protected abstract void engineSpecificClearAction();

    protected abstract <K, V, T extends Cache<K, V>> T cache(Class<? extends OBJ> clazz);

    public OBJ save(OBJ obj) {
        return save(obj, obj.getKey());
    }

    public OBJ save(OBJ obj, Object key) {
        if (key == null)
            throw new NullPointerException();
        getMap(obj.getClass()).put(key, obj);
        return obj;
    }

    public void delete(OBJ obj) {
        OBJ removed = getMap(obj.getClass()).remove(obj.getKey());
        if (removed != null)
            deleted.add(removed);
    }

    @SuppressWarnings("unchecked")
    protected void revert(OBJ obj) {
//        unSave(obj);
        //todo
        cache(obj.getClass()).remove(obj.getKey());
    }

    protected Map<Object, OBJ> getMap(Class<? extends OBJ> clazz) {
        return scope.computeIfAbsent(clazz, k -> new HashMap<>());
    }

    public void rollback() {
        try {
            for (Map.Entry<Object, Object> memento : mementos.entrySet()) {
                OBJ src = getMementoValue(memento);
                Object dst = getMap(src.getClass()).get(memento.getKey());
                if (dst != null)
                    Beans.set(meta, src, dst);
            }
            engineSpecificRollbackAction();
        } catch (IllegalAccessException | InvocationTargetException | IntrospectionException e) {
            throw new RuntimeException(e);
        } finally {
            clear();
        }
    }

    private void commit() {
        try {
            for (Map.Entry<Class<? extends OBJ>, Map<Object, OBJ>> byClass : scope.entrySet()) {
                cache(byClass.getKey()).putAll(byClass.getValue());
            }
            for (OBJ obj : deleted) {
                cache(obj.getClass()).remove(obj.getKey());
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

    @SuppressWarnings("unchecked")
    public <T extends Context> T transaction(Transactionable transactionable) {
        startTransactionIfNotStarted();
        try {
            transactionable.commit();
            commit();
        } catch (Exception e) {
            rollback();
            throw new RuntimeException(e);
        }
        return (T) this;
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

    public <K, V extends OBJ> V getAndClose(Class<V> clazz, K identity) {
        V value = get(clazz, identity);
        close();
        return value;
    }

    @FunctionalInterface
    protected interface Worker {
        /**
         * Performs this operation on the given arguments.
         *
         * @param c   class
         * @param map (key -> value)
         */
//        <K, V>
        void accept(Class<? extends OBJ> c, Map<Object, OBJ> map, boolean isTombstone) throws IllegalAccessException;

    }
}
