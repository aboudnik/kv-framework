package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.Context;
import org.boudnik.framework.test.core.TestEntry;
import org.junit.Test;

import static org.junit.Assert.*;

public class CreateDeleteTest extends TransactionTest {

    @Test
    public void testCreateDeleteCommit() {
        Context tx = Context.instance();
        tx.transaction(() -> {
            TestEntry te = new TestEntry("testCreateDeleteCommit");
            te.save();
            te.delete();
        });
        tx.transaction(() -> assertNull(tx.get(TestEntry.class, "testCreateDeleteCommit")));
    }

    @Test
    public void testCreateDeleteRollback() {
        Context tx = Context.instance();
        tx.transaction(() -> {
            TestEntry te = new TestEntry("testCreateDeleteRollback");
            TestEntry saveResult = te.save();
            assertNotNull(saveResult);
            te.delete();
            tx.rollback();
        });
        tx.transaction(() -> {
            TestEntry testCreateDeleteRollback = tx.get(TestEntry.class, "testCreateDeleteRollback");
            assertNull(testCreateDeleteRollback);
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
        } catch (RuntimeException e) {
            assertEquals("Rollback Exception", e.getMessage());
            tx.transaction(() -> assertNull(tx.get(TestEntry.class, "testCreateDeleteRollback")));
        }
    }
}
