package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.Context;
import org.boudnik.framework.test.core.TestEntry;
import org.junit.Test;

import static org.junit.Assert.*;


public class CreateSaveDeleteTest extends TransactionTest {

    @Test
    public void testCommitDeleteCommit() {
        Context tx = Context.instance();
        tx.transaction(() -> new TestEntry("testCommitDeleteCommit").save());
        tx.transaction(() -> assertNotNull(tx.get(TestEntry.class, "testCommitDeleteCommit")));

        tx.transaction(() -> {
            TestEntry entry = tx.get(TestEntry.class, "testCommitDeleteCommit");
            assertNotNull(entry);
            entry.delete();
        });

        tx.transaction(() -> assertNull(tx.get(TestEntry.class, "testCommitDeleteCommit")));
    }

    @Test
    public void testCommitDeleteRollback() {
        Context tx = Context.instance();
        tx.
                transaction(() -> new TestEntry("testCommitDeleteRollback").save()).
                transaction(() -> assertNotNull(tx.get(TestEntry.class, "testCommitDeleteRollback"))).
                transaction(() -> {
                    TestEntry entry = tx.get(TestEntry.class, "testCommitDeleteRollback");
                    assertNotNull(entry);
                    entry.delete();
                    tx.rollback();
                }).
                transaction(() -> assertNotNull(tx.get(TestEntry.class, "testCommitDeleteRollback")));
    }

    @Test
    public void testCommitDeleteRollbackViaException() {
        Context tx = Context.instance();
        tx.
                transaction(() -> new TestEntry("testCommitDeleteRollback").save()).
                transaction(() -> assertNotNull(tx.get(TestEntry.class, "testCommitDeleteRollback")));

        try {
            tx.transaction(() -> {
                TestEntry entry = tx.get(TestEntry.class, "testCommitDeleteRollback");
                assertNotNull(entry);
                entry.delete();
                throw new RuntimeException("Rollback Exception");
            });
        } catch (RuntimeException e) {
            assertEquals("Rollback Exception", e.getMessage());
        } finally {
            tx.transaction(() -> assertNotNull(tx.get(TestEntry.class, "testCommitDeleteRollback")));
        }
    }
}
