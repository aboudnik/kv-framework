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

public class GetDeleteComplexEntryTest {

    @BeforeClass
    public static void beforeAll() {
        if (TransactionFactory.getCurrentTransaction() == null) {
            TransactionFactory.getOrCreateTransaction(CacheProvider.IGNITE, () -> new IgniteTransaction(Ignition.getOrStart(new IgniteConfiguration())), true)
                    .withCache(ComplexTestEntry2.class);
        }
    }

    @Test
    public void testGetDeleteCommitComplexTestEntry2() {
        Context tx = Context.instance();
        ComplexTestEntry2 te = new ComplexTestEntry2(new ComplexTestEntry(new TestEntry("testGetDeleteCommitComplex")));
        tx.transaction(te);

        ComplexTestEntry key = te.getKey();
        ComplexTestEntry2 entry = tx.get(ComplexTestEntry2.class, key);
        Assert.assertNotNull(entry);
        tx.transaction(entry::delete);
        Assert.assertNull(tx.getAndClose(ComplexTestEntry2.class, key));
    }

    @Test
    public void testGetDeleteRollbackComplexTestEntry2() {
        Context tx = Context.instance();
        ComplexTestEntry2 te = new ComplexTestEntry2(new ComplexTestEntry(new TestEntry("testCommitDeleteRollback")));
        tx.transaction(te);

        ComplexTestEntry key = te.getKey();
        ComplexTestEntry2 entry = tx.get(ComplexTestEntry2.class, key);
        Assert.assertNotNull(entry);

        entry.delete();
        tx.rollback();
        Assert.assertNotNull(tx.getAndClose(ComplexTestEntry2.class, key));
    }

    @Test(expected = RuntimeException.class)
    public void testGetDeleteRollbackViaExceptionComplexTestEntry2() {
        Context tx = Context.instance();
        ComplexTestEntry2 te = new ComplexTestEntry2(new ComplexTestEntry(new TestEntry("testCommitDeleteRollback")));
        tx.transaction(te);

        ComplexTestEntry key = te.getKey();
        ComplexTestEntry2 entry = tx.get(ComplexTestEntry2.class, key);
        Assert.assertNotNull(entry);

        tx.transaction(() -> {
            entry.delete();
            throw new RuntimeException("RollbackException");
        });
        Assert.assertNotNull(tx.getAndClose(ComplexTestEntry2.class, key));
    }
}
