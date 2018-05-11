package org.boudnik.framework;

import org.boudnik.framework.util.Beans;

import javax.cache.Cache;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Alexandre_Boudnik
 * @since 11/15/2017
 */
public abstract class Context implements AutoCloseable {
    protected final Beans beans = new Beans();
    private boolean rollback = false;

    enum State {
        NEW,
        CURRENT,
        DEAD
    }

    class Cell<V extends OBJ<?>> {
        final V obj;
        final V memento;
        State state;

        Cell(V obj, State state, V memento) {
            this.obj = obj;
            this.memento = memento;
            this.state = state;
        }

        void revert() {
            beans.set(memento, obj);
        }

        boolean isDirty() {
            return !beans.equals(memento, obj);
        }

        void setDeleted() {
            this.state = State.DEAD;
        }
    }

    private final Map cells = new HashMap();
    private int tranCount = 0;

    protected abstract void startTransactionIfNotStarted();

    protected abstract boolean isTransactionExist();

    protected abstract void engineSpecificCommitAction();

    protected abstract void engineSpecificRollbackAction();

    protected abstract void engineSpecificClearAction();

    public abstract <K, V extends OBJ<K>> Cache<K, V> cache(Class<? extends OBJ> clazz);

    protected abstract <K, V extends OBJ<K>> V toObject(Object external, K identity);

    private <K, V extends OBJ<K>> Cell<V> getCell(V obj) {
        return this.<K, V>getMap(obj.getClass()).get(obj.getKey());
    }

    private <K, V extends OBJ<K>> Map<K, Cell<V>> getMap(Class<? extends OBJ> clazz) {
        return this.<K, V>getScope().computeIfAbsent(clazz, cls -> new HashMap<>());
    }

    public final <K, V extends OBJ<K>> V get(Class<V> clazz, K key) {
        if (tranCount == 0)
            throw new TenacityException("get(class, key) has been called outside of transaction boundary");
        Map<K, Cell<V>> map = getMap(clazz);
        Cell<V> cell = map.get(key);
        if (cell == null)
            try {
                Object external = cache(clazz).get(key);
                if (external == null)
                    return null;
                V v = toObject(external, key);
                this.<K, V>getMap(v.getClass()).put(key, new Cell<>(v, State.CURRENT, beans.clone(v)));
                return v;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        else
            return cell.obj;
    }

    <K, V extends OBJ<K>> V save(V obj) {
        if (tranCount == 0)
            throw new TenacityException("save() has been called outside of transaction boundary");
        return save(obj, obj.getKey());
    }

    private <K, V extends OBJ<K>> V save(V obj, K key) {
        if (key == null)
            throw new NullPointerException();
        @SuppressWarnings("unchecked") Class<V> clazz = (Class<V>) obj.getClass();
        Map<K, Cell<V>> map = getMap(clazz);
        Cell<V> cell = map.get(key);
        if (cell == null) {
            V wasHere = get(clazz, key);
            map.put(key, wasHere == null ? new Cell<>(obj, State.NEW, beans.clone(obj)) : new Cell<>(obj, State.CURRENT, wasHere));
        }
        return obj;
    }

    <K, V extends OBJ<K>> void delete(V obj) {
        if (tranCount == 0)
            throw new TenacityException("delete() has been called outside of transaction boundary");
        getCell(obj).setDeleted();
    }


    @SuppressWarnings("unchecked")
    private <K, V extends OBJ<K>> Map<Class<? extends OBJ>, Map<K, Cell<V>>> getScope() {
        return (Map<Class<? extends OBJ>, Map<K, Cell<V>>>) cells;
    }

    private <K, V extends OBJ<K>> void commit() {
        Set<K> toRemove = new HashSet<>();
        Map<K, V> toPut = new HashMap<>();
        for (Map.Entry<Class<? extends OBJ>, Map<K, Cell<V>>> byClass : this.<K, V>getScope().entrySet()) {
            toRemove.clear();
            toPut.clear();
            Cache<K, V> cache = cache(byClass.getKey());
            for (Map.Entry<K, Cell<V>> entry : byClass.getValue().entrySet()) {
                K key = entry.getKey();
                Cell<V> cell = entry.getValue();
                if (cell.state == State.DEAD)
                    toRemove.add(key);
                else if (cell.state == State.NEW || cell.isDirty())
                    toPut.put(key, cell.obj);
            }
            cache.removeAll(toRemove);
            cache.putAll(toPut);
        }
        engineSpecificCommitAction();
    }

    public void rollback() {
        if (tranCount == 0)
            throw new TenacityException("rollback() has been called outside of transaction boundary");
        rollback = true;
        tranCount = 0;
    }

    private <K, V extends OBJ<K>> void doRollback() {
        for (Map.Entry<Class<? extends OBJ>, Map<K, Cell<V>>> byClass : this.<K, V>getScope().entrySet()) {
            for (Map.Entry<K, Cell<V>> entry : byClass.getValue().entrySet()) {
                entry.getValue().revert();
            }
        }
        engineSpecificRollbackAction();
        cells.clear();
    }

    @Deprecated
    public Context transaction(OBJ obj) {
        return transaction(obj::save);
    }

    public Context transaction(Transactionable transactionable) {
        try {
            tranCount = 1;
            startTransactionIfNotStarted();
            transactionable.commit();
            if (rollback)
                doRollback();
            else
                commit();
        } catch (Exception e) {
            doRollback();
            throw e;
        } finally {
            engineSpecificClearAction();
            cells.clear();
            tranCount = 0;
            rollback = false;
        }
        return this;
    }

    <K, V extends OBJ<K>> boolean isDeleted(V reference) {
        return getScope().get(reference.getClass()).get(reference.getKey()).state == State.DEAD;
    }

    public static Context instance() {
        return TransactionFactory.getCurrentTransaction();
    }

    @Override
    public void close() {
        if (isTransactionExist())
            rollback();
    }

    @Deprecated
    public <K, V extends OBJ<K>> V getAndClose(Class<V> clazz, K identity) {
        V value = get(clazz, identity);
        close();
        return value;
    }
}
