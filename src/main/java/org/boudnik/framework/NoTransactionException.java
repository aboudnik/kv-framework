package org.boudnik.framework;

import org.apache.ignite.transactions.TransactionException;

/**
 * @author Alexandre_Boudnik
 * @since 11/21/17 14:52
 */
public class NoTransactionException extends TransactionException {
    NoTransactionException() {
        super("No transaction in progress");
    }
}
