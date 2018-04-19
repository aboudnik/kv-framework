package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.CacheProvider;
import org.boudnik.framework.Transaction;
import org.boudnik.framework.test.core.TestEntry;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class CreateDeleteTest extends TransactionTest {

    public CreateDeleteTest(CacheProvider input) {
        super(input, TestEntry.class);
    }

    @Test
    public void testCreateDeleteCommit() {
        Transaction tx = Transaction.instance();
        tx.txCommit(() -> {
            TestEntry te = new TestEntry("testCreateDeleteCommit");
            te.save();
            te.delete();
        });
        Assert.assertNull(tx.getAndClose(TestEntry.class, "testCreateDeleteCommit"));
    }

    @Test
    public void testCreateDeleteRollback() {
        Transaction tx = Transaction.instance();
        TestEntry te = new TestEntry("testCreateDeleteRollback");
        TestEntry saveResult = te.save();
        Assert.assertNotNull(saveResult);
        te.delete();
        tx.rollback();
        Assert.assertNull(tx.getAndClose(TestEntry.class, "testCreateDeleteRollback"));
    }

    @Test(expected = RuntimeException.class)
    public void testCreateDeleteRollbackViaException() {
        Transaction tx = Transaction.instance();
        tx.txCommit(() -> {
            TestEntry te = new TestEntry("testCreateDeleteRollback");
            te.save();
            te.delete();
            throw new RuntimeException("Rollback Exception");
        });
        Assert.assertNull(tx.getAndClose(TestEntry.class, "testCreateDeleteRollback"));
    }
}
