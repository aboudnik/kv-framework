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

public class GetDeleteTest{

    @BeforeClass
    public static void beforeAll() {
        if (TransactionFactory.getCurrentTransaction() == null) {
            TransactionFactory.getOrCreateTransaction(CacheProvider.IGNITE, () -> new IgniteTransaction(Ignition.getOrStart(new IgniteConfiguration())), true)
                    .withCache(MutableTestEntry.class);
        }
    }

    @Test
    public void testGetDeleteCommit() {
        Context tx = Context.instance();
        tx.transaction(new MutableTestEntry("testGetDeleteCommit"));

        MutableTestEntry entry = tx.get(MutableTestEntry.class, "testGetDeleteCommit");
        Assert.assertNotNull(entry);

        tx.transaction(entry::delete);
        Assert.assertNull(tx.get(MutableTestEntry.class, "testGetDeleteCommit"));
    }

    @Test
    public void testGetDeleteRollback() {
        Context tx = Context.instance();
        tx.transaction(new MutableTestEntry("testGetDeleteRollback"));

        MutableTestEntry entry = tx.get(MutableTestEntry.class, "testGetDeleteRollback");
        Assert.assertNotNull(entry);

        entry.delete();
        tx.rollback();
        Assert.assertNotNull(tx.get(MutableTestEntry.class, "testGetDeleteRollback"));
    }

    @Test(expected = RuntimeException.class)
    public void testGetDeleteRollbackViaException() {
        Context tx = Context.instance();
        tx.transaction(new MutableTestEntry("testGetDeleteRollback"));

        MutableTestEntry entry = tx.get(MutableTestEntry.class, "testGetDeleteRollback");
        Assert.assertNotNull(entry);
        tx.transaction(() -> {
            entry.delete();
            throw new RuntimeException("Rollback Exception");
        });

        Assert.assertNotNull(tx.get(MutableTestEntry.class, "testGetDeleteRollback"));
    }
}
