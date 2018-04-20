package org.boudnik.framework;

import org.boudnik.framework.hazelcast.HazelcastTransaction;
import org.boudnik.framework.ignite.IgniteTransaction;

public enum CacheProvider {
    IGNITE(IgniteTransaction.class),
    HAZELCAST(HazelcastTransaction.class);

    private final Class<? extends Transaction> transactionClass;

    <T extends Transaction> CacheProvider(Class<T> transactionClass) {
        this.transactionClass = transactionClass;
    }

    public Class<? extends Transaction> getTransactionClass() {
        return transactionClass;
    }
}
