package org.boudnik.framework;

import org.apache.ignite.binary.BinaryObject;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author Alexandre_Boudnik
 * @since 11/13/17 15:41
 */
public interface OBJ<K> extends Serializable {

    default K getKey() {
        throw new NoSuchElementException("getKey");
    }

    default void setKey(@NotNull K key) {
//        throw new NoSuchElementException("setKey");
    }

    default void save() {
        Transaction.instance().save(this);
    }

    default void save(K key) {
        Transaction.instance().save(this, key);
    }

    default void delete() {
        Transaction.instance().delete(this);
    }

    default void revert() {
        Transaction.instance().revert(this);
    }

    default void onCommit(BinaryObject current, BinaryObject memento) {

    }

    default void onRollback() {

    }

    OBJ<Object> TOMBSTONE = new OBJ<Object>() {
    };

    class REF<I, V extends OBJ<I>> {
        private final Class<V> clazz;
        private transient V reference;

        private I identity;

        public REF(Class<V> clazz) {
            this.clazz = clazz;
        }

        public V get() {
            if (reference == null) {
                return this.reference = Transaction.instance().get(clazz, identity);
            } else if (Transaction.instance().isDeleted(reference))
                return null;
            else
                return reference;
        }

        public void set(V reference) {
            setIdentity(reference == null ? null : reference.getKey());
            setReference(reference);
        }

        private void setReference(V reference) {
            this.reference = reference;
        }

        private void setIdentity(I identity) {
            this.identity = identity;
        }

        @Override
        public String toString() {
            return "REF{" +
                    "clazz=" + clazz +
                    ", reference=" + reference +
                    ", identity=" + identity +
                    '}';
        }
    }
    abstract class Implementation<K> implements OBJ<K> {


        private transient K key;

        protected Implementation(K key) {
            this.key = key;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public void setKey(@NotNull K key) {
            this.key = key;
        }
    }

    abstract class Historical<K> extends Implementation<K> {
        public static final String REF = Implementation.REF.class.getName();
        List<History> history = new ArrayList<>();

        public Historical(K key) {
            super(key);
        }

        @Override
        public void onCommit(BinaryObject current, BinaryObject memento) {
            for (String field : current.type().fieldNames()) {
                Object c = current.field(field);
                Object m = memento.field(field);
                if (m == null) {
                    if (c != null)
                        System.out.printf("%s:%s = null%n", field, c);
                } else if (!m.equals(c)) {
                    if (m instanceof BinaryObject) {
                        if (((BinaryObject) c).type().typeName().equals(REF)) {
                            Object ci = getIdentity(c);
                            Object mi = getIdentity(m);
                            if (!ci.equals(mi))
                                System.out.printf("%s:%s = %s%n", field, ci, mi);
                        }
                    } else
                        System.out.printf("%s:%s = %s%n", field, c, m);
                }
            }
        }
    }

    static Object getIdentity(Object o) {
        return ((BinaryObject) o).field("identity");
    }
}
