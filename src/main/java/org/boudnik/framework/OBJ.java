package org.boudnik.framework;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
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

    @SuppressWarnings("unchecked")
    default <T> T save() {
        return (T) Context.instance().save(this);
    }

    @SuppressWarnings("unchecked")
    default <T> T save(K key) {
        return (T) Context.instance().save(this, key);
    }

    default void delete() {
        Context.instance().delete(this);
    }

    //  default void revert() {
    //      Transaction.instance().revert(this);
    //  }

    default void onCommit(Object current, Object memento) {

    }

    default void onRollback() {

    }

    OBJ<Object> TOMBSTONE = new OBJ<Object>() {
    };

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

/*    abstract class Historical<K> extends Implementation<K> {
        static final String REF = Implementation.REF.class.getName();
        List<History> history = new ArrayList<>();

        public Historical(K key) {
            super(key);
        }

        @Override
        public void onCommit(BinaryObject instance, BinaryObject memento) {
            for (String field : instance.type().fieldNames()) {
                Object c = instance.field(field);
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
    }*/

    class REF<I, V extends OBJ<I>> implements Serializable {
        private final Class<V> clazz;
        private transient V reference;

        private I identity;

        public REF(Class<V> clazz) {
            this.clazz = clazz;
        }

        /**
         * Implements simple state machine; when reference not in scope, obtains it from store.
         *
         * @return cached {@link OBJ.REF#reference}
         */
        public V get() {
            if (reference == null)
                return identity == null ? null : (reference = Context.instance().get(clazz, identity));
            V inScope = Context.instance().get(clazz, reference.getKey());
            if (Context.isDeleted(inScope))
                return null;
            if (inScope != reference)
                return reference = Context.instance().get(clazz, identity);
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
}
