package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.CacheProvider;
import org.boudnik.framework.Transaction;
import org.boudnik.framework.test.core.TestEntry;
import org.junit.Assert;
import org.junit.Test;

public class CreateSaveTest extends TransactionTest {

    public CreateSaveTest(CacheProvider input) {
        super(input, TestEntry.class);
    }

    @Test
    public void testCreateSaveCommit() {
        Transaction tx = Transaction.instance();
        tx.txCommit(new TestEntry("testCreateSaveCommit"));
        Assert.assertNotNull(tx.getAndClose(TestEntry.class, "testCreateSaveCommit"));
    }

    @Test
    public void testCreateSaveRollback() {
        Transaction tx = Transaction.instance();
        TestEntry saveResult = new TestEntry("testCreateSaveRollback").save();
        Assert.assertNotNull(saveResult);
        tx.rollback();
        Assert.assertNull(tx.getAndClose(TestEntry.class, "testCreateSaveRollback"));
    }

    @Test(expected = RuntimeException.class)
    public void testCreateSaveRollbackViaException() {
        Transaction tx = Transaction.instance();
        tx.txCommit(() -> {
            new TestEntry("testCreateSaveRollback").save();
            throw new RuntimeException("Rollback Exception");
        });
        Assert.assertNull(tx.getAndClose(TestEntry.class, "testCreateSaveRollback"));
    }
}
