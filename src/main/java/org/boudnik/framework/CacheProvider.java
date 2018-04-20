package org.boudnik.framework;

import com.hazelcast.core.Hazelcast;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.boudnik.framework.hazelcast.HazelcastTransaction;
import org.boudnik.framework.ignite.IgniteTransaction;

import java.util.function.Supplier;

public enum CacheProvider {
    IGNITE(IgniteTransaction.class, () -> new IgniteTransaction(Ignition.getOrStart(new IgniteConfiguration()))),
    HAZELCAST(HazelcastTransaction.class, () -> new HazelcastTransaction(Hazelcast.newHazelcastInstance()));

    private final Class<? extends Transaction> transactionClass;
    private final Supplier<? extends Transaction> defaultSupplier;

    <T extends Transaction> CacheProvider(Class<T> transactionClass, Supplier<T> supplier){
        this.defaultSupplier = supplier;
        this.transactionClass = transactionClass;
    }

    public Class<? extends Transaction> getTransactionClass() {
        return transactionClass;
    }

    public Supplier<? extends Transaction> getDefaultSupplier() {
        return defaultSupplier;
    }
}
