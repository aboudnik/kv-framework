package org.boudnik.framework.test;

import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.boudnik.framework.CacheProvider;
import org.boudnik.framework.Context;
import org.boudnik.framework.TransactionFactory;
import org.boudnik.framework.ignite.IgniteTransaction;
import org.boudnik.framework.test.core.TestEntry;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ObjSaveTest {

    @BeforeClass
    public static void beforeAll() {
        if (TransactionFactory.getCurrentTransaction() == null) {
            TransactionFactory.getOrCreateTransaction(CacheProvider.IGNITE, () -> new IgniteTransaction(Ignition.getOrStart(new IgniteConfiguration())), true)
                    .withCache(TestEntry.class);
        }
    }

    @Test
    public void checkSeveralEntriesWithDifferentKeys() {
        Context tx = Context.instance();
        tx.transaction(() -> {
            new TestEntry("http://localhost/1").save("checkSeveralEntriesWithDifferentKeys");
            new TestEntry("http://localhost/1").save();
        });

        TestEntry entry = tx.get(TestEntry.class, "http://localhost/1");
        Assert.assertNotNull(entry);
        entry = tx.get(TestEntry.class, "checkSeveralEntriesWithDifferentKeys");
        Assert.assertNotNull(entry);
    }
}
