package org.boudnik.framework;

import java.util.function.Supplier;

public class TransactionFactory {
    private static final ThreadLocal<Context> TRANSACTION_THREAD_LOCAL = new ThreadLocal<>();

    private TransactionFactory() {
    }

    @SuppressWarnings("unchecked")
    static <T extends Context> T getCurrentTransaction() {
        return (T) TRANSACTION_THREAD_LOCAL.get();
    }

    public static <T extends Context> T getOrCreateTransaction(CacheProvider cacheProvider, Supplier<T> supplier) {
        return getOrCreateTransaction(cacheProvider, supplier, false);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Context> T getOrCreateTransaction(CacheProvider cacheProvider, Supplier<T> supplier, boolean abortCurrentIfAnotherProviderPresent) {
        if (supplier == null)
            throw new NullPointerException();
        Context context = TRANSACTION_THREAD_LOCAL.get();
        if (context != null) {
            if (context.getClass().isAssignableFrom(cacheProvider.getTransactionClass())) return (T) context;
            else if (!abortCurrentIfAnotherProviderPresent) {
                throw new RuntimeException("Another provider transaction is present " + context);
            } else {
                TRANSACTION_THREAD_LOCAL.get().close();
            }
        }

        context = supplier.get();
        TRANSACTION_THREAD_LOCAL.set(context);
        return (T) context;
    }
}
