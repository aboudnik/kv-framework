package org.boudnik.framework.test.testsuites;

import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.boudnik.framework.CacheProvider;
import org.boudnik.framework.Context;
import org.boudnik.framework.TransactionFactory;
import org.boudnik.framework.ignite.IgniteTransaction;
import org.boudnik.framework.test.core.MutableTestEntry;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class GetUpdateSaveTest {

    private static final String NEW_VALUE = "New Value";

    @BeforeClass
    public static void beforeAll() {
        if (TransactionFactory.getCurrentTransaction() == null) {
            TransactionFactory.getOrCreateTransaction(CacheProvider.IGNITE, () -> new IgniteTransaction(Ignition.getOrStart(new IgniteConfiguration())), true)
                    .withCache(MutableTestEntry.class);
        }
    }

    @Test
    public void testGetUpdateSaveCommit() {
        Context tx = Context.instance();
        tx.transaction(new MutableTestEntry("testGetUpdateSaveCommit"));

        final MutableTestEntry entry = tx.get(MutableTestEntry.class, "testGetUpdateSaveCommit");
        Assert.assertNotNull(entry);
        Assert.assertNull(entry.getValue());

        tx.transaction(() -> {
            entry.setValue(NEW_VALUE);
            entry.save();
        });
        Assert.assertEquals(NEW_VALUE, tx.get(MutableTestEntry.class, "testGetUpdateSaveCommit").getValue());
    }

    @Test
    public void testGetUpdateSaveRollback() {
        Context tx = Context.instance();
        tx.transaction(new MutableTestEntry("testGetUpdateSaveRollback"));

        final MutableTestEntry entry = tx.get(MutableTestEntry.class, "testGetUpdateSaveRollback");
        Assert.assertNotNull(entry);
        Assert.assertNull(entry.getValue());

        entry.setValue(NEW_VALUE);
        MutableTestEntry saveResult = entry.save();
        Assert.assertNotNull(saveResult);
        tx.rollback();
        Assert.assertNull(tx.get(MutableTestEntry.class, "testGetUpdateSaveRollback").getValue());
    }

    @Test(expected = RuntimeException.class)
    public void testGetUpdateSaveRollbackViaException() {
        Context tx = Context.instance();
        tx.transaction(new MutableTestEntry("testGetUpdateSaveRollback"));

        final MutableTestEntry entry = tx.get(MutableTestEntry.class, "testGetUpdateSaveRollback");
        Assert.assertNotNull(entry);
        Assert.assertNull(entry.getValue());

        tx.transaction(() -> {
            entry.setValue(NEW_VALUE);
            entry.save();
            throw new RuntimeException("Rollback Exception");
        });
        Assert.assertNull(tx.get(MutableTestEntry.class, "testGetUpdateSaveRollback").getValue());
    }
}
