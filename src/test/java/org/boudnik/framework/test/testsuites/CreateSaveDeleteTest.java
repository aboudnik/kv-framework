package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.Context;
import org.boudnik.framework.test.core.TestEntry;
import org.junit.Assert;
import org.junit.Test;


public class CreateSaveDeleteTest extends TransactionTest {

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
        try {
            tx.transaction(() -> {
                entry.delete();
                throw new RuntimeException("Rollback Exception");
            });
        } finally {
            Assert.assertNotNull(tx.getAndClose(TestEntry.class, "testCommitDeleteRollback"));
        }
    }
}
