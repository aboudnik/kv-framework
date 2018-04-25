package org.boudnik.framework;

import java.util.function.Supplier;

public class TransactionFactory {
    private static final ThreadLocal<Context> TRANSACTION_THREAD_LOCAL = new ThreadLocal<>();

    private TransactionFactory() {
    }

    @SuppressWarnings("unchecked")
    public static <T extends Context> T getCurrentTransaction() {
        return (T) TRANSACTION_THREAD_LOCAL.get();
    }

    public static <T extends Context> T getOrCreateTransaction(Class<T> transactionClass, Supplier<T> supplier) {
        return getOrCreateTransaction(transactionClass, supplier, false);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Context> T getOrCreateTransaction(Class<T> transactionClass, Supplier<T> supplier, boolean abortCurrentIfAnotherProviderPresent) {
        if (supplier == null)
            throw new NullPointerException();
        Context context = TRANSACTION_THREAD_LOCAL.get();
        if (context != null) {
            if (context.getClass().isAssignableFrom(transactionClass)) return (T) context;
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
