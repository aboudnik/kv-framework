package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.Transaction;
import org.boudnik.framework.test.core.TestEntry;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class CreateSaveDeleteTest {

    @BeforeClass
    public static void beforeAll(){
        Transaction.instance().withCache(TestEntry.class);
    }

    @Test
    public void testCommitDeleteCommit() {
        Transaction tx = Transaction.instance();
        tx.txCommit(new TestEntry("testCommitDeleteCommit"));

        TestEntry entry = tx.get(TestEntry.class, "testCommitDeleteCommit");
        Assert.assertNotNull(entry);

        tx.txCommit(entry::delete);
        Assert.assertNull(tx.getAndClose(TestEntry.class, "testCommitDeleteCommit"));
    }

    @Test
    public void testCommitDeleteRollback() {
        Transaction tx = Transaction.instance();
        tx.txCommit(new TestEntry("testCommitDeleteRollback"));

        TestEntry entry = tx.get(TestEntry.class, "testCommitDeleteRollback");
        Assert.assertNotNull(entry);

        entry.delete();
        tx.rollback();
        Assert.assertNotNull(tx.getAndClose(TestEntry.class, "testCommitDeleteRollback"));
    }

    @Test(expected = RuntimeException.class)
    public void testCommitDeleteRollbackViaException() {
        Transaction tx = Transaction.instance();
        tx.txCommit(new TestEntry("testCommitDeleteRollback"));

        TestEntry entry = tx.get(TestEntry.class, "testCommitDeleteRollback");
        Assert.assertNotNull(entry);

        tx.txCommit(() -> {
            entry.delete();
            throw  new RuntimeException("RollbackException");
        });
        Assert.assertNotNull(tx.getAndClose(TestEntry.class, "testCommitDeleteRollback"));
    }
}
