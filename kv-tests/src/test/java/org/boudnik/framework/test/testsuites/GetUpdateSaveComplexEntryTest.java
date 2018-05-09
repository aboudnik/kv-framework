package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.Context;
import org.boudnik.framework.test.core.ComplexTestEntry;
import org.boudnik.framework.test.core.ComplexTestEntry2;
import org.boudnik.framework.test.core.TestEntry;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * fixme setKey!!!
 */
public class GetUpdateSaveComplexEntryTest extends TransactionTest {

    private static final String NEW_VALUE = "New Value";

    @Test
    public void testGetUpdateSaveCommitComplexTestEntry() {
        Context tx = Context.instance();
        ComplexTestEntry2 te = new ComplexTestEntry2(new ComplexTestEntry(new TestEntry("testUpdateCommit")));
        tx.transaction(te::save);

        ComplexTestEntry key = te.getKey();
        tx.transaction(() -> {
            ComplexTestEntry2 entry = tx.get(ComplexTestEntry2.class, key);
            assertNotNull(entry);
            entry.setValue(NEW_VALUE);
            entry.getKey().setValue(NEW_VALUE);
            entry.save();
        });
        tx.transaction(() -> {
            ComplexTestEntry2 updatedEntry = tx.get(ComplexTestEntry2.class, key);
            assertNotNull(updatedEntry);
            assertEquals(NEW_VALUE, updatedEntry.getValue());
            assertEquals(NEW_VALUE, updatedEntry.getKey().getValue());
        });
    }

    @Ignore
    @Test
    public void testGetUpdateSaveRollbackComplexTestEntry() {
        Context tx = Context.instance();
        ComplexTestEntry2 te = new ComplexTestEntry2(new ComplexTestEntry(new TestEntry("testUpdateRollback")));
        tx.transaction(te::save);

        ComplexTestEntry key = te.getKey();
        tx.transaction(() -> {
            ComplexTestEntry2 entry = tx.get(ComplexTestEntry2.class, key);
            assertNotNull(entry);
            entry.setValue(NEW_VALUE);
            entry.getKey().setValue(NEW_VALUE);
            ComplexTestEntry2 saveResult = entry.save();
            assertNotNull(saveResult);
            tx.rollback();
        });

        tx.transaction(() -> {
            ComplexTestEntry2 notUpdatedEntry = tx.get(ComplexTestEntry2.class, key);
            assertNotNull(notUpdatedEntry);
            assertNull(notUpdatedEntry.getValue());
            assertNull(notUpdatedEntry.getKey().getValue());
        });
    }

    @Ignore
    @Test
    public void testGetUpdateSaveRollbackViaExceptionComplexTestEntry() {
        Context tx = Context.instance();
        ComplexTestEntry2 te = new ComplexTestEntry2(new ComplexTestEntry(new TestEntry("testUpdateRollback")));
        tx.transaction(te::save);

        ComplexTestEntry key = te.getKey();
        try {
            tx.transaction(() -> {
                ComplexTestEntry2 entry = tx.get(ComplexTestEntry2.class, key);
                assertNotNull(entry);
                entry.setValue(NEW_VALUE);
                entry.getKey().setValue(NEW_VALUE);
                entry.save();
                throw new RuntimeException("Rollback Exception");
            });
        } catch (RuntimeException e) {
            assertEquals("Rollback Exception", e.getMessage());
        } finally {
            tx.transaction(() -> {
                ComplexTestEntry2 notUpdatedEntry = tx.get(ComplexTestEntry2.class, key);
                assertNotNull(notUpdatedEntry);
                assertNull(notUpdatedEntry.getKey().getValue());
                assertNull(notUpdatedEntry.getValue());
            });
        }
    }
}
