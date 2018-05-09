package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.Context;
import org.boudnik.framework.test.core.TestEntry;
import org.junit.Test;

import static org.junit.Assert.*;

public class CreateSaveTest extends TransactionTest {

    @Test
    public void testCreateSaveCommit() {
        Context tx = Context.instance();
        tx.transaction(() -> {
            new TestEntry("testCreateSaveCommit").save();
            assertNotNull(tx.get(TestEntry.class, "testCreateSaveCommit"));
        });
    }

    @Test
    public void testCreateSaveRollback() {
        Context tx = Context.instance();
        tx.transaction(() -> {
            TestEntry saveResult = new TestEntry("testCreateSaveRollback").save();
            assertNotNull(saveResult);
            tx.rollback();
        });
        tx.transaction(() -> assertNull(tx.get(TestEntry.class, "testCreateSaveRollback")));
    }

    @Test
    public void testCreateSaveRollbackViaException() {
        Context tx = Context.instance();
        try {
            tx.transaction(() -> {
                new TestEntry("testCreateSaveRollbackViaException").save();
                throw new RuntimeException("Rollback Exception");
            });
        } catch (RuntimeException e) {
            assertEquals("Rollback Exception", e.getMessage());
        } finally {
            tx.transaction(() -> assertNull(tx.get(TestEntry.class, "testCreateSaveRollbackViaException")));
        }
    }
}
