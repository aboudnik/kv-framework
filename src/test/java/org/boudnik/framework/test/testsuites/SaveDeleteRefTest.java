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

import static org.junit.Assert.*;

public class SaveDeleteRefTest {

    private RefTestEntry ref;

    @BeforeClass
    public static void beforeAll() {
        if (TransactionFactory.getCurrentTransaction() == null) {
            TransactionFactory.getOrCreateTransaction(CacheProvider.IGNITE, () -> new IgniteTransaction(Ignition.getOrStart(new IgniteConfiguration())), true)
                    .withCache(RefTestEntry.class, TestEntry.class);
        }
    }

    @Test
    public void testSaveDeleteCommit() {
        Context tx = Context.instance();
        tx.transaction(() -> {
            TestEntry entry = new TestEntry("CreateSaveDeleteCommitRef").save();
            ref = new RefTestEntry("CreateSaveDeleteCommitRef", entry).save();
        });

        tx.transaction(() -> {
            TestEntry actual = tx.get(TestEntry.class, "CreateSaveDeleteCommitRef");
            actual.delete();
        });

        assertNull(tx.get(TestEntry.class, "CreateSaveDeleteCommitRef"));
        RefTestEntry actualRef = tx.get(RefTestEntry.class, ref.getKey());
        assertNull(actualRef.getEntry());
    }

    @Test
    public void testSaveDeleteRollback() {
        Context tx = Context.instance();
        tx.transaction(() -> {
            TestEntry entry = new TestEntry("CreateSaveDeleteRollbackRef").save();
            ref = new RefTestEntry("CreateSaveDeleteRollbackRef", entry).save();
        });

        TestEntry actual = tx.get(TestEntry.class, "CreateSaveDeleteRollbackRef");
        actual.delete();
        tx.rollback();

        TestEntry actualEntry = tx.get(TestEntry.class, "CreateSaveDeleteRollbackRef");
        assertNotNull(actualEntry);
        RefTestEntry actualRef = tx.get(RefTestEntry.class, ref.getKey());
        assertNotNull(actualRef);
        assertSame(actualRef.getEntry(), actualEntry);
    }
}
