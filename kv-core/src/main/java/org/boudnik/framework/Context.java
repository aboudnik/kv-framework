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

    enum State {
        NEW {
            @Override
            void load() {
            }

            @Override
            void save() {
            }

            @Override
            void kill() {
            }
        },
        HOLLY {
            @Override
            void load() {
            }

            @Override
            void save() {
            }

            @Override
            void kill() {
            }
        },
        READ {
            @Override
            void load() {
            }

            @Override
            void save() {
            }

            @Override
            void kill() {
            }
        },
        DIRTY {
            @Override
            void load() {
            }

            @Override
            void save() {
            }

            @Override
            void kill() {
            }
        },
        DEAD;

        void load() {
        }

        void save() {
        }

        void kill() {

        }
    }

    class Cell<V extends OBJ<?>> {
        final V obj;
        V memento;
        State state = State.NEW;

        Cell(V obj) {
            memento = beans.clone(this.obj = obj);
        }

        void revert() {
            state.load();
        }

        boolean isDirty() {
            return !beans.equals(memento, obj);
        }

        public void setState(State state) {
            this.state = state;
        }
    }

    private final Map cells = new HashMap();
    private final Set<OBJ> deleted = new HashSet<>();
    private final Map<Object, Object> mementos = new HashMap<>();
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

    private <K, V extends OBJ<K>> V putCell(K key, V obj, State state) {
        this.<K, V>getMap(obj.getClass()).put(key, new Cell<>(obj));
        return obj;
    }

    private <K, V extends OBJ<K>> Map<K, Cell<V>> getMap(Class<? extends OBJ> clazz) {
        return this.<K, V>getScope().computeIfAbsent(clazz, cls -> new HashMap<>());
    }

    public final <K, V extends OBJ<K>> V get(Class<V> clazz, K identity) {
        Map<K, Cell<V>> map = getMap(clazz);
        Cell<V> cell = map.get(identity);
        if (cell == null)
            try {
                Object external = getNative(clazz, identity);
                if (external == null)
                    return null;
                V v = toObject(external, identity);
                putCell(identity, v, State.HOLLY);
                return v;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        else
            return cell.obj;
    }

    public <K, V extends OBJ<K>> V save(V obj) {
        return save(obj, obj.getKey());
    }

    public <K, V extends OBJ<K>> V save(V obj, K key) {
        if (key == null)
            throw new NullPointerException();
        return putCell(key, obj, State.HOLLY);
    }

    public <K, V extends OBJ<K>> void delete(V obj) {
        getCell(obj).setState(State.DEAD);
    }


    @SuppressWarnings("unchecked")
    private <K, V extends OBJ<K>> Map<Class<? extends OBJ>, Map<K, Cell<V>>> getScope() {
        return (Map<Class<? extends OBJ>, Map<K, Cell<V>>>) cells;
    }

    @SuppressWarnings("unchecked")
    private <K> Map<K, Object> getMementos() {
        return (Map<K, Object>) mementos;
    }

    private <K, V extends OBJ<K>> void commit() {
        if (tranCount == 0)
            throw new TenacityException();
        Set<K> toRemove = new HashSet<>();
        Map<K, V> toPut = new HashMap<>();
        for (Map.Entry<Class<? extends OBJ>, Map<K, Cell<V>>> byClass : this.<K, V>getScope().entrySet()) {
            Cache<K, V> cache = cache(byClass.getKey());
            for (Map.Entry<K, Cell<V>> entry : byClass.getValue().entrySet()) {
                K key = entry.getKey();
                Cell<V> cell = entry.getValue();
                if (cell.deleted)
                    toRemove.add(key);
                else if (cell.isDirty())
                    toPut.put(key, cell.obj);
            }
            cache.removeAll(toRemove);
            toRemove.clear();
            cache.putAll(toPut);
            toPut.clear();
        }
        engineSpecificCommitAction();
    }

    public <K, V extends OBJ<K>> void rollback() {
        if (tranCount == 0)
            throw new TenacityException();
        for (Map.Entry<Class<? extends OBJ>, Map<K, Cell<V>>> byClass : this.<K, V>getScope().entrySet()) {
            Cache<K, V> cache = cache(byClass.getKey());
            for (Map.Entry<K, Cell<V>> entry : byClass.getValue().entrySet()) {
                entry.getValue().revert();
            }
        }
        engineSpecificRollbackAction();
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
            tranCount++;
            startTransactionIfNotStarted();
            transactionable.commit();
            commit();
        } catch (Exception e) {
            rollback();
            throw new TenacityException(e);
        } finally {
            engineSpecificClearAction();
            cells.clear();
            mementos.clear();
            deleted.clear();
            tranCount--;
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
