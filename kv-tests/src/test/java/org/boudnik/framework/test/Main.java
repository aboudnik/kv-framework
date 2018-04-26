package org.boudnik.framework.test;

import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.boudnik.framework.Context;
import org.boudnik.framework.TransactionFactory;
import org.boudnik.framework.ignite.IgniteTransaction;
import org.boudnik.framework.test.core.TestEntry;
import org.junit.Assert;
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
    public static void beforeAll() {
        TransactionFactory.getOrCreateTransaction(IgniteTransaction.class, () -> new IgniteTransaction(Ignition.getOrStart(new IgniteConfiguration())), true)
                .withCache(TestEntry.class);
    }

    @Test
    public void main() {
        Context tx = Context.instance();
        tx.transaction(() -> new TestEntry("http://localhost/1").save(""));
    }

    @Test
    public void mt() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        for (int i = 0; i < 2; i++) {
            executor.submit(() -> {
                try {
                    Context tx = TransactionFactory.getOrCreateTransaction(IgniteTransaction.class, () -> new IgniteTransaction(Ignition.getOrStart(new IgniteConfiguration())), true).withCache(TestEntry.class);
                    tx.transaction(() -> new TestEntry("http://localhost/1").save(""));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        try {
            executor.awaitTermination(2, TimeUnit.SECONDS);
            Assert.assertNotNull(Context.instance().get(TestEntry.class, ""));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}