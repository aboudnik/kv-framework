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

public class MultipleClassesWithSameKeyTest {

    @BeforeClass
    public static void beforeAll() {
        if (TransactionFactory.getCurrentTransaction() == null) {
            TransactionFactory.getOrCreateTransaction(CacheProvider.IGNITE, () -> new IgniteTransaction(Ignition.getOrStart(new IgniteConfiguration())), true)
                    .withCache(ComplexTestEntry2.class, TestEntry.class);
        }
    }

    @Test
    public void MultipleEntriesWithSameKey() {
        Context tx = Context.instance();
        ComplexTestEntry2 te = new ComplexTestEntry2(new ComplexTestEntry(new TestEntry("Some key")));
        tx.transaction(te);
        tx.transaction(new TestEntry("Some key"));

        ComplexTestEntry key = te.getKey();
        ComplexTestEntry2 entry = tx.get(ComplexTestEntry2.class, key);
        Assert.assertNotNull(entry);
        tx.transaction(entry::delete);
        Assert.assertNull(tx.getAndClose(ComplexTestEntry2.class, key));
        Assert.assertNotNull(tx.getAndClose(TestEntry.class, "Some key"));
    }

    @Test
    public void MultipleEntriesWithSameKey2() {
        Context tx = Context.instance();
        ComplexTestEntry2 te = new ComplexTestEntry2(new ComplexTestEntry(new TestEntry("Other key")));
        tx.transaction(te);
        tx.transaction(new TestEntry("Other key"));

        ComplexTestEntry key = te.getKey();
        TestEntry entry = tx.get(TestEntry.class, "Other key");
        Assert.assertNotNull(entry);
        tx.transaction(entry::delete);
        Assert.assertNull(tx.getAndClose(TestEntry.class, "Other key"));
        Assert.assertNotNull(tx.getAndClose(ComplexTestEntry2.class, key));
    }
}
