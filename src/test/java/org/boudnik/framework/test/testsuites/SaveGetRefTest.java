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

public class SaveGetRefTest {

    @BeforeClass
    public static void beforeAll() {
        TransactionFactory.getInstance().getOrCreateIgniteTransaction(() -> Ignition.getOrStart(new IgniteConfiguration()), true).withCache(RefTestEntry.class, TestEntry.class);
    }

    @Test
    public void testSaveGetCommit() {
        Transaction tx = Transaction.instance();
        TestEntry entry = new TestEntry("SaveGetCommitRef");
        RefTestEntry ref = new RefTestEntry("SaveGetCommitRef", entry);
        tx.txCommit(() -> {
            entry.save();
            ref.save();
            assertSame(tx.get(TestEntry.class, "SaveGetCommitRef"), entry);
            assertSame(ref.getEntry(), entry);
        });

        tx.txCommit(() -> {
            TestEntry actual = tx.get(TestEntry.class, "SaveGetCommitRef");
            TestEntry expected = ref.getEntry();
            assertSame(actual, expected);
            RefTestEntry actualRef = tx.get(RefTestEntry.class, ref.getKey());
            assertSame(actualRef.getEntry(), ref.getEntry());
        });
    }

    @Test
    public void testSaveGetRollback() {
        Transaction tx = Transaction.instance();
        TestEntry entry = new TestEntry("SaveGetRollbackRef").save();
        RefTestEntry ref = new RefTestEntry("SaveGetRollbackRef", entry).save();

        tx.rollback();

        assertNull(tx.get(TestEntry.class, "SaveGetRollbackRef"));
        assertNull(tx.get(RefTestEntry.class, ref.getKey()));
    }
}
