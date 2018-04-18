package org.boudnik.framework.test;

import org.boudnik.framework.Transaction;
import org.boudnik.framework.TransactionFactory;
import org.boudnik.framework.test.core.TestEntry;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Alexandre_Boudnik
 * @since 02/13/2018
 */
public class Main {

    @BeforeClass
    public static void beforeAll(){
        TransactionFactory.getInstance().getOrCreateIgniteTransaction().withCache(TestEntry.class);
    }

    @Test
    public void main() {
        Transaction tx = Transaction.instance();
        tx.txCommit(() -> new TestEntry("http://localhost/1").save(""));
    }

    @Test
    public void mt() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        for(int i = 0; i < 2; i++) {
            executor.submit(() -> {
                Transaction tx = Transaction.instance();
                tx.txCommit(() -> new TestEntry("http://localhost/1").save(""));
            });
        }

        try {
            executor.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
        }
    }
}
