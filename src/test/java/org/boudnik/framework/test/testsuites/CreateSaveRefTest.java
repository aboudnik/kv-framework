package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.CacheProvider;
import org.boudnik.framework.Transaction;
import org.boudnik.framework.TransactionFactory;
import org.boudnik.framework.ignite.IgniteTransaction;
import org.boudnik.framework.test.core.RefTestEntry;
import org.boudnik.framework.test.core.TestEntry;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertSame;

public class CreateSaveRefTest {

    private RefTestEntry ref;

    @BeforeClass
    public static void beforeAll() {
        TransactionFactory.<IgniteTransaction>getOrCreateTransaction(CacheProvider.IGNITE, true).withCache(RefTestEntry.class, TestEntry.class);
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
