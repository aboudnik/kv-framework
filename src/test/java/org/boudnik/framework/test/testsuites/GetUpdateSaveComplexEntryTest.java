package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.Context;
import org.boudnik.framework.test.core.ComplexTestEntry;
import org.boudnik.framework.test.core.ComplexTestEntry2;
import org.boudnik.framework.test.core.TestEntry;
import org.junit.Assert;
import org.junit.Test;

public class GetUpdateSaveComplexEntryTest extends TransactionTest {

    private static final String NEW_VALUE = "New Value";

    @Test
    public void testGetUpdateSaveCommitComplexTestEntry() {
        Context tx = Context.instance();
        ComplexTestEntry2 te = new ComplexTestEntry2(new ComplexTestEntry(new TestEntry("testUpdateCommit")));
        tx.transaction(te);

        ComplexTestEntry key = te.getKey();
        ComplexTestEntry2 entry = tx.get(ComplexTestEntry2.class, key);
        Assert.assertNotNull(entry);

        tx.transaction(() -> {
            entry.setValue(NEW_VALUE);
            entry.getKey().setValue(NEW_VALUE);
            entry.save();
        });
        ComplexTestEntry2 updatedEntry = tx.getAndClose(ComplexTestEntry2.class, key);
        Assert.assertEquals(NEW_VALUE, updatedEntry.getValue());
        Assert.assertEquals(NEW_VALUE, updatedEntry.getKey().getValue());
    }

    @Test
    public void testGetUpdateSaveRollbackComplexTestEntry() {
        Context tx = Context.instance();
        ComplexTestEntry2 te = new ComplexTestEntry2(new ComplexTestEntry(new TestEntry("testUpdateRollback")));
        tx.transaction(te);

        ComplexTestEntry key = te.getKey();
        ComplexTestEntry2 entry = tx.get(ComplexTestEntry2.class, key);
        Assert.assertNotNull(entry);

        entry.setValue(NEW_VALUE);
        entry.getKey().setValue(NEW_VALUE);
        ComplexTestEntry2 saveResult = entry.save();
        Assert.assertNotNull(saveResult);
        tx.rollback();
        ComplexTestEntry2 notUpdatedEntry = tx.getAndClose(ComplexTestEntry2.class, key);
        Assert.assertNull(notUpdatedEntry.getValue());
        Assert.assertNull(notUpdatedEntry.getKey().getValue());
    }

    @Test(expected = RuntimeException.class)
    public void testGetUpdateSaveRollbackViaExceptionComplexTestEntry() {
        Context tx = Context.instance();
        ComplexTestEntry2 te = new ComplexTestEntry2(new ComplexTestEntry(new TestEntry("testUpdateRollback")));
        tx.transaction(te);

        ComplexTestEntry key = te.getKey();
        ComplexTestEntry2 entry = tx.get(ComplexTestEntry2.class, key);
        Assert.assertNotNull(entry);
        try {
            tx.transaction(() -> {
                entry.setValue(NEW_VALUE);
                entry.getKey().setValue(NEW_VALUE);
                entry.save();
                throw new RuntimeException("Rollback Exception");
            });
        } finally {
            ComplexTestEntry2 notUpdatedEntry = tx.getAndClose(ComplexTestEntry2.class, key);
            Assert.assertNull(notUpdatedEntry.getKey().getValue());
            Assert.assertNull(notUpdatedEntry.getValue());
        }
    }
}
