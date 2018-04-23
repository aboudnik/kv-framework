package org.boudnik.framework.test.testsuites;

import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.boudnik.framework.CacheProvider;
import org.boudnik.framework.Context;
import org.boudnik.framework.TransactionFactory;
import org.boudnik.framework.ignite.IgniteTransaction;
import org.boudnik.framework.test.core.RefTestEntry;
import org.boudnik.framework.test.core.TestEntry;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class SaveGetRefTest {
    @BeforeClass
    public static void beforeAll() {
        if (TransactionFactory.getCurrentTransaction() == null) {
            TransactionFactory.getOrCreateTransaction(CacheProvider.IGNITE, () -> new IgniteTransaction(Ignition.getOrStart(new IgniteConfiguration())), true)
                    .withCache(RefTestEntry.class, TestEntry.class);
        }
    }

    @Test
    public void testSaveGetCommit() {
        Context tx = Context.instance();
        TestEntry entry = new TestEntry("SaveGetCommitRef");
        RefTestEntry ref = new RefTestEntry("SaveGetCommitRef", entry);
        tx.transaction(() -> {
            entry.save();
            ref.save();
            assertSame(tx.get(TestEntry.class, "SaveGetCommitRef"), entry);
            assertSame(ref.getEntry(), entry);
        });

        TestEntry actual = tx.get(TestEntry.class, "SaveGetCommitRef");
        TestEntry expected = ref.getEntry();
        assertSame(actual, expected);
        RefTestEntry actualRef = tx.get(RefTestEntry.class, ref.getKey());
        assertSame(actualRef.getEntry(), ref.getEntry());
    }

    @Test
    public void testSaveGetRollback() {
        Context tx = Context.instance();
        TestEntry entry = new TestEntry("SaveGetRollbackRef").save();
        RefTestEntry ref = new RefTestEntry("SaveGetRollbackRef", entry).save();

        tx.rollback();

        assertNull(tx.get(TestEntry.class, "SaveGetRollbackRef"));
        assertNull(tx.get(RefTestEntry.class, ref.getKey()));
    }
}
