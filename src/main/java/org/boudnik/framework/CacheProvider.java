package org.boudnik.framework;

import org.boudnik.framework.hazelcast.HazelcastTransaction;
import org.boudnik.framework.ignite.IgniteTransaction;

public enum CacheProvider {
    IGNITE(IgniteTransaction.class),
    HAZELCAST(HazelcastTransaction.class);

    private final Class<? extends Context> transactionClass;

    <T extends Context> CacheProvider(Class<T> transactionClass) {
        this.transactionClass = transactionClass;
    }

    public Class<? extends Context> getTransactionClass() {
        return transactionClass;
    }
}
