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
    //todo: make it private again
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

        public void setState(State state) {
            this.state = state;
        }
    }

    private final Map cells = new HashMap();
    private int tranCount = 0;

    protected abstract <K> Object getNative(Class<? extends OBJ> clazz, K identity) throws Exception;

    protected abstract void startTransactionIfNotStarted();

    protected abstract boolean isTransactionExist();

    protected abstract void engineSpecificCommitAction();

    protected abstract void engineSpecificRollbackAction();

    protected abstract void engineSpecificClearAction();

    public abstract <K, V extends OBJ<K>> Cache<K, V> cache(Class<? extends OBJ> clazz);

    protected abstract <K, V extends OBJ<K>> V toObject(Object external, K identity) throws Exception;

    private <K, V extends OBJ<K>> V getMementoValue(Map.Entry<K, Object> memento, Object value) throws Exception {
        return toObject(value, memento.getKey());
    }

    private <K, V extends OBJ<K>> Cell<V> getCell(V obj) {
        return this.<K, V>getMap(obj.getClass()).get(obj.getKey());
    }

    private <K, V extends OBJ<K>> Map<K, Cell<V>> getMap(Class<? extends OBJ> clazz) {
        return this.<K, V>getScope().computeIfAbsent(clazz, cls -> new HashMap<>());
    }

    public final <K, V extends OBJ<K>> V get(Class<V> clazz, K key) {
        if (tranCount == 0)
            throw new TenacityException();
        Map<K, Cell<V>> map = getMap(clazz);
        Cell<V> cell = map.get(key);
        if (cell == null)
            try {
                Object external = getNative(clazz, key);
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
            throw new TenacityException();
        return save(obj, obj.getKey());
    }

    private <K, V extends OBJ<K>> V save(V obj, K key) {
        if (key == null)
            throw new NullPointerException();
        @SuppressWarnings("unchecked") Class<V> clazz = (Class<V>) obj.getClass();
        V wasHere = get(clazz, key);
        Map<K, Cell<V>> map = getMap(clazz);
        map.put(key, wasHere == null ? new Cell<>(obj, State.NEW, beans.clone(obj)) : new Cell<>(obj, State.CURRENT, wasHere));
        return obj;
    }

    <K, V extends OBJ<K>> void delete(V obj) {
        if (tranCount == 0)
            throw new TenacityException();
        getCell(obj).setState(State.DEAD);
    }


    @SuppressWarnings("unchecked")
    private <K, V extends OBJ<K>> Map<Class<? extends OBJ>, Map<K, Cell<V>>> getScope() {
        return (Map<Class<? extends OBJ>, Map<K, Cell<V>>>) cells;
    }

    private <K, V extends OBJ<K>> void commit() {
        if (tranCount == 0)
            throw new TenacityException();
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

    public <K, V extends OBJ<K>> void rollback() {
        if (tranCount == 0)
            throw new TenacityException();
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

    // TODO: 04/30/2018
    //    }
    //        cache(obj.getClass()).remove(obj.getKey());
    //        unSave(obj);
    //    protected <K, V extends OBJ<K>> void revert(V obj) {

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
            if (e instanceof TenacityException)
                throw e;
            else
                throw new TenacityException(e);
        } finally {
            engineSpecificClearAction();
            cells.clear();
            tranCount = 0;
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

    public <K, V extends OBJ<K>> V getAndClose(Class<V> clazz, K identity) {
        V value = get(clazz, identity);
        close();
        return value;
    }
}
