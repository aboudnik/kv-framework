package org.boudnik.framework;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.boudnik.framework.hazelcast.HazelcastTransaction;
import org.boudnik.framework.ignite.IgniteTransaction;

import java.util.function.Supplier;

public class TransactionFactory {
    private static final ThreadLocal<Transaction> TRANSACTION_THREAD_LOCAL = new ThreadLocal<>();

    private TransactionFactory() {
    }

    private static class TransactionFactoryHolder {
        private static final TransactionFactory INSTANCE = new TransactionFactory();
    }

    public static TransactionFactory getInstance() {
        return TransactionFactoryHolder.INSTANCE;
    }

    public static Transaction getTransaction(){
        return TRANSACTION_THREAD_LOCAL.get();
    }

    public IgniteTransaction getOrCreateIgniteTransaction(Supplier<Ignite> supplier) {
        return getOrCreateIgniteTransaction(supplier, false);
    }

    public IgniteTransaction getOrCreateIgniteTransaction(Supplier<Ignite> supplier, boolean abortCurrent) {
        Transaction transaction = TRANSACTION_THREAD_LOCAL.get();
        if (transaction != null) {
            if (transaction instanceof IgniteTransaction) return (IgniteTransaction) transaction;
            else if (!abortCurrent) {
                throw new RuntimeException("Not Hazelcast Transaction is present " + transaction);
            } else {
                TRANSACTION_THREAD_LOCAL.get().close();
            }
        }

        IgniteTransaction igniteTransaction = new IgniteTransaction(supplier.get());
        TRANSACTION_THREAD_LOCAL.set(igniteTransaction);
        return igniteTransaction;
    }

    public IgniteTransaction getOrCreateIgniteTransaction() {
        return getOrCreateIgniteTransaction(() -> Ignition.getOrStart(new IgniteConfiguration()), false);
    }

    public HazelcastTransaction getOrCreateHazelcastTransaction() {
        return getOrCreateHazelcastTransaction(Hazelcast::newHazelcastInstance, false);
    }

    public HazelcastTransaction getOrCreateHazelcastTransaction(Supplier<HazelcastInstance> supplier) {
        return getOrCreateHazelcastTransaction(supplier, false);
    }

    public HazelcastTransaction getOrCreateHazelcastTransaction(Supplier<HazelcastInstance> supplier, boolean abortCurrent) {
        Transaction transaction = TRANSACTION_THREAD_LOCAL.get();
        if (transaction != null) {
            if (transaction instanceof HazelcastTransaction) return (HazelcastTransaction) transaction;
            else if (!abortCurrent) {
                throw new RuntimeException("Not Hazelcast Transaction is present " + transaction);
            } else {
                TRANSACTION_THREAD_LOCAL.get().close();
            }
        }

        HazelcastTransaction hazelcastTransaction = new HazelcastTransaction(supplier.get());
        TRANSACTION_THREAD_LOCAL.set(hazelcastTransaction);
        return hazelcastTransaction;
    }
}
