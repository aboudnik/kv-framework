package org.boudnik.framework;


import java.io.Serializable;

@FunctionalInterface
public interface Transactionable extends Serializable {
    void commit();
}
