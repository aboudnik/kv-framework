package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.Context;
import org.boudnik.framework.test.core.TestEntry;
import org.junit.Assert;
import org.junit.Test;

public class CreateDeleteTest extends TransactionTest {

    @Test
    public void testCreateDeleteCommit() {
        Context tx = Context.instance();
        tx.transaction(() -> {
            TestEntry te = new TestEntry("testCreateDeleteCommit");
            te.save();
            te.delete();
        });
        Assert.assertNull(tx.getAndClose(TestEntry.class, "testCreateDeleteCommit"));
    }

    @Test
    public void testCreateDeleteRollback() {
        Context tx = Context.instance();
        TestEntry te = new TestEntry("testCreateDeleteRollback");
        TestEntry saveResult = te.save();
        Assert.assertNotNull(saveResult);
        te.delete();
        tx.rollback();
        Assert.assertNull(tx.getAndClose(TestEntry.class, "testCreateDeleteRollback"));
    }

    @Test(expected = RuntimeException.class)
    public void testCreateDeleteRollbackViaException() {
        Context tx = Context.instance();
        try {
            tx.transaction(() -> {
                TestEntry te = new TestEntry("testCreateDeleteRollback");
                te.save();
                te.delete();
                throw new RuntimeException("Rollback Exception");
            });
        } finally {
            Assert.assertNull(tx.getAndClose(TestEntry.class, "testCreateDeleteRollback"));
        }
    }
}
