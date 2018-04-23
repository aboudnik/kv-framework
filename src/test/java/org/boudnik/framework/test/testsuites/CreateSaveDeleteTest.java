package org.boudnik.framework.test.testsuites;

import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.boudnik.framework.CacheProvider;
import org.boudnik.framework.Context;
import org.boudnik.framework.TransactionFactory;
import org.boudnik.framework.ignite.IgniteTransaction;
import org.boudnik.framework.test.core.TestEntry;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


public class CreateSaveDeleteTest {

    @BeforeClass
    public static void beforeAll() {
        if (TransactionFactory.getCurrentTransaction() == null) {
            TransactionFactory.getOrCreateTransaction(CacheProvider.IGNITE, () -> new IgniteTransaction(Ignition.getOrStart(new IgniteConfiguration())), true).withCache(TestEntry.class);
        }
    }

    @Test
    public void testCommitDeleteCommit() {
        Context tx = Context.instance();
        tx.transaction(new TestEntry("testCommitDeleteCommit"));

        TestEntry entry = tx.get(TestEntry.class, "testCommitDeleteCommit");
        Assert.assertNotNull(entry);

        tx.transaction(entry::delete);
        Assert.assertNull(tx.getAndClose(TestEntry.class, "testCommitDeleteCommit"));
    }

    @Test
    public void testCommitDeleteRollback() {
        Context tx = Context.instance();
        tx.transaction(new TestEntry("testCommitDeleteRollback"));

        TestEntry entry = tx.get(TestEntry.class, "testCommitDeleteRollback");
        Assert.assertNotNull(entry);

        entry.delete();
        tx.rollback();
        Assert.assertNotNull(tx.getAndClose(TestEntry.class, "testCommitDeleteRollback"));
    }

    @Test(expected = RuntimeException.class)
    public void testCommitDeleteRollbackViaException() {
        Context tx = Context.instance();
        tx.transaction(new TestEntry("testCommitDeleteRollback"));

        TestEntry entry = tx.get(TestEntry.class, "testCommitDeleteRollback");
        Assert.assertNotNull(entry);

        tx.transaction(() -> {
            entry.delete();
            throw new RuntimeException("RollbackException");
        });
        Assert.assertNotNull(tx.getAndClose(TestEntry.class, "testCommitDeleteRollback"));
    }
}
