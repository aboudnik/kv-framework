package org.boudnik.framework.test;

import org.boudnik.framework.Context;
import org.boudnik.framework.test.core.TestEntry;
import org.boudnik.framework.test.testsuites.TransactionTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Alexandre_Boudnik
 * @since 02/13/2018
 */
public class MainTest extends TransactionTest {

    @Test
    public void main() {
        Context tx = Context.instance();
        tx.transaction(() -> new TestEntry("http://localhost/1").save());
    }

    @Test
    public void mt() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        for (int i = 0; i < 2; i++) {
            executor.submit(() -> {
                Context tx = getContext();
                tx.transaction(() -> new TestEntry("http://localhost/2").save());
            });
        }

        try {
            executor.awaitTermination(8, TimeUnit.SECONDS);
            Assert.assertNotNull(Context.instance().get(TestEntry.class, "http://localhost/2"));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private Context getContext() {
        switch (getProvider()) {
            case "Ignite":
                return Initializer.initIgnite();
            case "Hazelcast":
                return Initializer.initHazelcast();
            case "H2":
                return Initializer.initH2();
            default:
                return Initializer.initIgnite();
        }
    }
}
