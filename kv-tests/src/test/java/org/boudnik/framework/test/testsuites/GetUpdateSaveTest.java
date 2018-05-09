package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.Context;
import org.boudnik.framework.test.core.MutableTestEntry;
import org.junit.Test;

import static org.junit.Assert.*;

public class GetUpdateSaveTest extends TransactionTest {

    private static final String NEW_VALUE = "New Value";

    @Test
    public void testGetUpdateSaveCommit() {
        Context tx = Context.instance();
        tx.transaction(() -> new MutableTestEntry("testGetUpdateSaveCommit").save());

        tx.transaction(() -> {
            final MutableTestEntry entry = tx.get(MutableTestEntry.class, "testGetUpdateSaveCommit");
            assertNotNull(entry);
            assertNull(entry.getValue());
            entry.setValue(NEW_VALUE);
            entry.save();
        });
        tx.transaction(() -> {
            MutableTestEntry entry = tx.get(MutableTestEntry.class, "testGetUpdateSaveCommit");
            assertNotNull(entry);
            assertEquals(NEW_VALUE, entry.getValue());
        });
    }

    @Test
    public void testGetUpdateSaveRollback() {
        Context tx = Context.instance();
        tx.transaction(() -> new MutableTestEntry("testGetUpdateSaveRollback").save());

        tx.transaction(() -> {
            final MutableTestEntry entry = tx.get(MutableTestEntry.class, "testGetUpdateSaveRollback");
            assertNotNull(entry);
            assertNull(entry.getValue());

            entry.setValue(NEW_VALUE);
            MutableTestEntry saveResult = entry.save();
            assertNotNull(saveResult);
            tx.rollback();
        });
        tx.transaction(() -> {
            MutableTestEntry entry = tx.get(MutableTestEntry.class, "testGetUpdateSaveRollback");
            assertNotNull(entry);
            assertNull(entry.getValue());
        });
    }

    @Test
    public void testGetUpdateSaveRollbackViaException() {
        Context tx = Context.instance();
        tx.transaction(() -> new MutableTestEntry("testGetUpdateSaveRollback").save());

        try {
            tx.transaction(() -> {
                final MutableTestEntry entry = tx.get(MutableTestEntry.class, "testGetUpdateSaveRollback");
                assertNotNull(entry);
                assertNull(entry.getValue());
                entry.setValue(NEW_VALUE);
                entry.save();
                throw new RuntimeException("Rollback Exception");
            });
        } catch (RuntimeException e) {
            assertEquals("Rollback Exception", e.getMessage());
        } finally {
            tx.transaction(() -> {
                MutableTestEntry entry = tx.get(MutableTestEntry.class, "testGetUpdateSaveRollback");
                assertNotNull(entry);
                assertNull(entry.getValue());
            });
        }
    }
}
