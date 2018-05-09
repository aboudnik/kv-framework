package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.Context;
import org.boudnik.framework.test.core.MutableTestEntry;
import org.junit.Test;

import static org.junit.Assert.*;

public class GetUpdateSaveDeleteTest extends TransactionTest {

    private static final String NEW_VALUE = "New Value";

    @Test
    public void testGetUpdateSaveDeleteCommit() {
        Context tx = Context.instance();
        tx.transaction(() -> new MutableTestEntry("testGetUpdateSaveDeleteCommit").save());

        tx.transaction(() -> {
            final MutableTestEntry entry = tx.get(MutableTestEntry.class, "testGetUpdateSaveDeleteCommit");
            assertNotNull(entry);
            assertNull(entry.getValue());
            entry.setValue(NEW_VALUE);
            entry.save();
            entry.delete();
        });
        tx.transaction(() -> assertNull(tx.get(MutableTestEntry.class, "testGetUpdateSaveDeleteCommit")));
    }

    @Test
    public void testGetUpdateSaveDeleteRollback() {
        Context tx = Context.instance();
        tx.transaction(() -> new MutableTestEntry("testGetUpdateSaveDeleteRollback").save());

        tx.transaction(() -> {
            MutableTestEntry entry = tx.get(MutableTestEntry.class, "testGetUpdateSaveDeleteRollback");
            assertNotNull(entry);
            assertNull(entry.getValue());

            entry.setValue(NEW_VALUE);
            MutableTestEntry saveResult = entry.save();
            assertNotNull(saveResult);
            entry.delete();
            tx.rollback();
        });
        tx.transaction(() -> {
            MutableTestEntry entry = tx.get(MutableTestEntry.class, "testGetUpdateSaveDeleteRollback");
            assertNotNull(entry);
            assertNull(entry.getValue());
        });
    }

    @Test
    public void testGetUpdateSaveDeleteRollbackViaException() {
        Context tx = Context.instance();
        tx.transaction(() -> new MutableTestEntry("testGetUpdateSaveDeleteRollback").save());

        try {
            tx.transaction(() -> {
                final MutableTestEntry entry = tx.get(MutableTestEntry.class, "testGetUpdateSaveDeleteRollback");
                assertNotNull(entry);
                assertNull(entry.getValue());
                tx.transaction(() -> {
                    entry.setValue(NEW_VALUE);
                    entry.save();
                    entry.delete();
                    throw new RuntimeException("Rollback Exception");
                });
            });
        } catch (RuntimeException e){
            assertEquals("Rollback Exception", e.getMessage());
        } finally {
            tx.transaction(() -> {
                MutableTestEntry updatedEntry = tx.get(MutableTestEntry.class, "testGetUpdateSaveDeleteRollback");
                assertNotNull(updatedEntry);
                assertNull(updatedEntry.getValue());
            });
        }
    }
}
