package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.Context;
import org.boudnik.framework.test.core.MutableTestEntry;
import org.junit.Test;

import static org.junit.Assert.*;

public class GetDeleteTest extends TransactionTest {

    @Test
    public void testGetDeleteCommit() {
        Context tx = Context.instance();
        tx.transaction(() -> new MutableTestEntry("testGetDeleteCommit").save());

        tx.transaction(() -> {
            MutableTestEntry entry = tx.get(MutableTestEntry.class, "testGetDeleteCommit");
            assertNotNull(entry);
            entry.delete();
        });

        tx.transaction(() -> assertNull(tx.get(MutableTestEntry.class, "testGetDeleteCommit")));
    }

    @Test
    public void testGetDeleteRollback() {
        Context tx = Context.instance();
        tx.transaction(() -> new MutableTestEntry("testGetDeleteRollback").save());

        tx.transaction(() -> {
            MutableTestEntry entry = tx.get(MutableTestEntry.class, "testGetDeleteRollback");
            assertNotNull(entry);
            entry.delete();
            tx.rollback();
        });

        tx.transaction(() -> assertNotNull(tx.get(MutableTestEntry.class, "testGetDeleteRollback")));
    }

    @Test
    public void testGetDeleteRollbackViaException() {
        Context tx = Context.instance();
        tx.transaction(() -> new MutableTestEntry("testGetDeleteRollback").save());

        try {
            tx.transaction(() -> {
                MutableTestEntry entry = tx.get(MutableTestEntry.class, "testGetDeleteRollback");
                assertNotNull(entry);
                entry.delete();
                throw new RuntimeException("Rollback Exception");
            });
        } catch (RuntimeException e) {
            assertEquals("Rollback Exception", e.getMessage());
        } finally {
            tx.transaction(() -> assertNotNull(tx.get(MutableTestEntry.class, "testGetDeleteRollback")));
        }
    }
}
