package org.boudnik.framework;

@FunctionalInterface
public interface Transactionable extends Serializable {
    void commit();
}
