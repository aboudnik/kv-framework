package org.boudnik.framework.test.testsuites;

import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.boudnik.framework.Transaction;
import org.boudnik.framework.TransactionFactory;
import org.boudnik.framework.test.core.TestEntry;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class CreateDeleteTest {

    @BeforeClass
    public static void beforeAll(){

        TransactionFactory.getInstance().getOrCreateIgniteTransaction(() -> Ignition.getOrStart(new IgniteConfiguration()), true).withCache(TestEntry.class);
    }

    @Test
    public void testCreateDeleteCommit() {
        Transaction tx = Transaction.instance().txCommit(() -> {
            TestEntry te = new TestEntry("testCreateDeleteCommit");
            te.save();
            te.delete();
        });
        Assert.assertNull(tx.getAndClose(TestEntry.class, "testCreateDeleteCommit"));
    }

    @Test
    public void testCreateDeleteRollback() {
        Transaction tx = Transaction.instance();
        TestEntry te = new TestEntry("testCreateDeleteRollback");
        TestEntry saveResult = te.save();
        Assert.assertNotNull(saveResult);
        te.delete();
        tx.rollback();
        Assert.assertNull(tx.getAndClose(TestEntry.class, "testCreateDeleteRollback"));
    }

    @Test(expected = RuntimeException.class)
    public void testCreateDeleteRollbackViaException() {
        Transaction tx = Transaction.instance().txCommit(() -> {
            TestEntry te = new TestEntry("testCreateDeleteRollback");
            te.save();
            te.delete();
            throw new RuntimeException("Rollback Exception");
        });
        Assert.assertNull(tx.getAndClose(TestEntry.class, "testCreateDeleteRollback"));
    }
}
