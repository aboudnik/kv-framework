package org.boudnik.framework;

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

    @SuppressWarnings("unchecked")
    static <T extends Transaction> T getCurrentTransaction(){
        return (T) TRANSACTION_THREAD_LOCAL.get();
    }


    public static <T extends Transaction> T getOrCreateTransaction(CacheProvider cacheProvider) {
        return getOrCreateTransaction(cacheProvider, null, false);
    }

    public static <T extends Transaction> T getOrCreateTransaction(CacheProvider cacheProvider, boolean abortCurrent) {
        return getOrCreateTransaction(cacheProvider, null, abortCurrent);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Transaction> T getOrCreateTransaction(CacheProvider cacheProvider, Supplier<T> supplier, boolean abortCurrent) {
        Transaction transaction = TRANSACTION_THREAD_LOCAL.get();
        if (transaction != null) {
            if (transaction.getClass().isAssignableFrom(cacheProvider.getTransactionClass())) return (T) transaction;
            else if (!abortCurrent) {
                throw new RuntimeException("Another provider transaction is present " + transaction);
            } else {
                TRANSACTION_THREAD_LOCAL.get().close();
            }
        }

        transaction = supplier == null? cacheProvider.getDefaultSupplier().get(): supplier.get();
        TRANSACTION_THREAD_LOCAL.set(transaction);
        return (T) transaction;
    }
}
