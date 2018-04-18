package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.Transaction;
import org.boudnik.framework.test.core.ComplexTestEntry;
import org.boudnik.framework.test.core.ComplexTestEntry2;
import org.boudnik.framework.test.core.TestEntry;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class GetUpdateSaveComplexEntryTest {

    private static final String NEW_VALUE = "New Value";

    @BeforeClass
    public static void beforeAll(){
        Transaction.instance().withCache(ComplexTestEntry2.class);
    }

    @Test
    public void testGetUpdateSaveCommitComplexTestEntry() {
        Transaction tx = Transaction.instance();
        ComplexTestEntry2 te = new ComplexTestEntry2(new ComplexTestEntry(new TestEntry("testUpdateCommit")));
        tx.txCommit(te);

        ComplexTestEntry key = te.getKey();
        ComplexTestEntry2 entry = tx.get(ComplexTestEntry2.class, key);
        Assert.assertNotNull(entry);

        tx.txCommit(() -> {
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
        Transaction tx = Transaction.instance();
        ComplexTestEntry2 te = new ComplexTestEntry2(new ComplexTestEntry(new TestEntry("testUpdateRollback")));
        tx.txCommit(te);

        ComplexTestEntry key = te.getKey();
        ComplexTestEntry2 entry = tx.get(ComplexTestEntry2.class, key);
        Assert.assertNotNull(entry);

        entry.setValue(NEW_VALUE);
        entry.getKey().setValue(NEW_VALUE);
        entry.save();
        tx.rollback();
        ComplexTestEntry2 notUpdatedEntry = tx.getAndClose(ComplexTestEntry2.class, key);
        Assert.assertNull(notUpdatedEntry.getValue());
        Assert.assertNull(notUpdatedEntry.getKey().getValue());
    }

    @Test(expected = RuntimeException.class)
    public void testGetUpdateSaveRollbackViaExceptionComplexTestEntry() {
        Transaction tx = Transaction.instance();
        ComplexTestEntry2 te = new ComplexTestEntry2(new ComplexTestEntry(new TestEntry("testUpdateRollback")));
        tx.txCommit(te);

        ComplexTestEntry key = te.getKey();
        ComplexTestEntry2 entry = tx.get(ComplexTestEntry2.class, key);
        Assert.assertNotNull(entry);

        tx.txCommit(() -> {
            entry.setValue(NEW_VALUE);
            entry.getKey().setValue(NEW_VALUE);
            entry.save();
            throw new RuntimeException("Rollback Exception");
        });
        ComplexTestEntry2 notUpdatedEntry = tx.getAndClose(ComplexTestEntry2.class, key);
        Assert.assertNull(notUpdatedEntry.getKey().getValue());
        Assert.assertNull(notUpdatedEntry.getValue());
    }
}
