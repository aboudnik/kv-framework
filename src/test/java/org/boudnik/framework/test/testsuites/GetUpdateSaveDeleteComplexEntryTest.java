package org.boudnik.framework.test.testsuites;

import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.boudnik.framework.CacheProvider;
import org.boudnik.framework.Context;
import org.boudnik.framework.TransactionFactory;
import org.boudnik.framework.ignite.IgniteTransaction;
import org.boudnik.framework.test.core.ComplexTestEntry;
import org.boudnik.framework.test.core.ComplexTestEntry2;
import org.boudnik.framework.test.core.TestEntry;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class GetUpdateSaveDeleteComplexEntryTest {

    private static final String NEW_VALUE = "New Value";

    @BeforeClass
    public static void beforeAll() {
        if (TransactionFactory.getCurrentTransaction() == null) {
            TransactionFactory.getOrCreateTransaction(CacheProvider.IGNITE, () -> new IgniteTransaction(Ignition.getOrStart(new IgniteConfiguration())), true)
                    .withCache(ComplexTestEntry2.class);
        }
    }

    @Test
    public void testGetUpdateSaveDeleteCommit() {
        Context tx = Context.instance();
        ComplexTestEntry2 te = new ComplexTestEntry2(new ComplexTestEntry(new TestEntry("testCommitDeleteCommit")));
        tx.transaction(te);

        ComplexTestEntry key = te.getKey();
        ComplexTestEntry2 entry = tx.get(ComplexTestEntry2.class, key);
        Assert.assertNotNull(entry);
        Assert.assertNull(entry.getKey().getValue());
        Assert.assertNull(entry.getValue());

        tx.transaction(() -> {
            entry.setValue(NEW_VALUE);
            entry.getKey().setValue(NEW_VALUE);
            entry.save();
            entry.delete();
        });
        ComplexTestEntry2 updatedEntry = tx.getAndClose(ComplexTestEntry2.class, key);
        Assert.assertEquals(NEW_VALUE, updatedEntry.getValue());
        Assert.assertEquals(NEW_VALUE, updatedEntry.getKey().getValue());
    }

    @Test
    public void testGetUpdateSaveDeleteRollback() {
        Context tx = Context.instance();
        ComplexTestEntry2 te = new ComplexTestEntry2(new ComplexTestEntry(new TestEntry("testCommitDeleteRollback")));
        tx.transaction(te);

        ComplexTestEntry key = te.getKey();
        ComplexTestEntry2 entry = tx.get(ComplexTestEntry2.class, key);
        Assert.assertNotNull(entry);
        Assert.assertNull(entry.getValue());
        Assert.assertNull(entry.getKey().getValue());

        entry.setValue(NEW_VALUE);
        entry.getKey().setValue(NEW_VALUE);
        ComplexTestEntry2 saveResult = entry.save();
        Assert.assertNotNull(saveResult);
        entry.delete();
        tx.rollback();
        ComplexTestEntry2 notUpdatedEntry = tx.getAndClose(ComplexTestEntry2.class, key);
        Assert.assertNull(notUpdatedEntry.getValue());
        Assert.assertNull(notUpdatedEntry.getKey().getValue());
    }

    @Test(expected = RuntimeException.class)
    public void testGetUpdateSaveDeleteRollbackViaException() {
        Context tx = Context.instance();
        ComplexTestEntry2 te = new ComplexTestEntry2(new ComplexTestEntry(new TestEntry("testCommitDeleteRollback")));
        tx.transaction(te);

        ComplexTestEntry key = te.getKey();
        ComplexTestEntry2 entry = tx.get(ComplexTestEntry2.class, key);
        Assert.assertNotNull(entry);

        tx.transaction(() -> {
            entry.setValue(NEW_VALUE);
            entry.getKey().setValue(NEW_VALUE);
            entry.save();
            entry.delete();
            throw new RuntimeException("Rollback Exception");
        });
        ComplexTestEntry2 notUpdatedEntry = tx.getAndClose(ComplexTestEntry2.class, key);
        Assert.assertNull(notUpdatedEntry.getKey().getValue());
        Assert.assertNull(notUpdatedEntry.getValue());
    }
}
