package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.Context;
import org.boudnik.framework.Transactionable;
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
        tx.transaction(() -> {
            Assert.assertNull(tx.get(TestEntry.class, "testCreateDeleteCommit"));
        });
    }

    @Test
    public void testCreateDeleteRollback() {
        Context tx = Context.instance();
        tx.transaction(() -> {
            TestEntry te = new TestEntry("testCreateDeleteRollback");
            TestEntry saveResult = te.save();
            Assert.assertNotNull(saveResult);
            te.delete();
            tx.rollback();
        });
        tx.transaction(() -> {
            TestEntry testCreateDeleteRollback = tx.get(TestEntry.class, "testCreateDeleteRollback");
            Assert.assertNull(testCreateDeleteRollback);
        });
    }

    @Test
    public void testCreateDeleteRollbackViaException() {
        Context tx = Context.instance();
        try {
            tx.transaction(() -> {
                TestEntry te = new TestEntry("testCreateDeleteRollback");
                te.save();
                te.delete();
                throw new RuntimeException("Rollback Exception");
            });
        } catch (Exception ignored) {
            tx.transaction(() -> {
                Assert.assertNull(tx.getAndClose(TestEntry.class, "testCreateDeleteRollback"));
            });
        }
    }
}
