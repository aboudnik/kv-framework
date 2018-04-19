package org.boudnik.framework.test.testsuites;

import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.boudnik.framework.Transaction;
import org.boudnik.framework.TransactionFactory;
import org.boudnik.framework.test.core.RefTestEntry;
import org.boudnik.framework.test.core.TestEntry;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class CreateSaveRefTest {

    private RefTestEntry ref;

    @BeforeClass
    public static void beforeAll() {
        TransactionFactory.getInstance().getOrCreateIgniteTransaction(() -> Ignition.getOrStart(new IgniteConfiguration()), true).withCache(RefTestEntry.class, TestEntry.class);
    }

    @Test
    public void testCreateSaveCommit() {
        Transaction tx = Transaction.instance();
        tx.txCommit(() -> {
            TestEntry entry = new TestEntry("test").save();
            ref = new RefTestEntry("test", entry).save();
            assertSame(tx.get(TestEntry.class, "test"), entry);
            assertSame(ref.getEntry(), entry);
        });

        tx.txCommit(() -> {
            TestEntry actual = tx.get(TestEntry.class, "test");
            TestEntry expected = ref.getEntry();
            assertSame(actual, expected);
        });
    }
}