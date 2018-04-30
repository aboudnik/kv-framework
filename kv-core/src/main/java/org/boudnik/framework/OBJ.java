package org.boudnik.framework;

import java.io.Serializable;

/**
 * @author Alexandre_Boudnik
 * @since 11/13/17 15:41
 */
public interface OBJ<K> extends Serializable {

    K getKey();

    void setKey(K key);

    @SuppressWarnings("unchecked")
    default <V extends OBJ<K>> V save() {
        return (V) Context.instance().save(this);
    }

    default void delete() {
        Context.instance().delete(this);
    }

// TODO: 04/30/2018
//    default void revert() {
//        Context.instance().revert(this);
//    }

    @SuppressWarnings("unused")
    default void onCommit(Object current, Object memento) {

    }

    @SuppressWarnings("unused")
    default void onRollback() {

    }

    abstract class Implementation<K> implements OBJ<K> {

        private transient K key;

        protected Implementation() {
        }

        protected Implementation(K key) {
            this.key = key;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public void setKey(K key) {
            this.key = key;
        }

    }

    class REF<K, V extends OBJ<K>> implements Serializable {
        private final Class<V> clazz;
        private transient V reference;
        private K identity;

        public REF(Class<V> clazz) {
            this.clazz = clazz;
        }

        /**
         * Implements simple state machine; when reference not in scope, obtains it from store.
         *
         * @return cached {@link OBJ.REF#reference}
         */
        public V get() {
            Context context = Context.instance();
            if (reference == null)
                return identity == null ? null : (reference = context.get(clazz, identity));
            V inScope = context.get(clazz, reference.getKey());
            if (context.isDeleted(inScope))
                return null;
            if (inScope != reference)
                return reference = context.get(clazz, identity);
            return reference;
        }

        public void set(V reference) {
            setIdentity(reference == null ? null : reference.getKey());
            setReference(reference);
        }

        private void setReference(V reference) {
            this.reference = reference;
        }

        private void setIdentity(K identity) {
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
