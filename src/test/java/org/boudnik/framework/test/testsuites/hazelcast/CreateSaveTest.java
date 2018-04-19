package org.boudnik.framework.test.testsuites.hazelcast;

import org.boudnik.framework.CacheProvider;
import org.boudnik.framework.Transaction;
import org.boudnik.framework.TransactionFactory;
import org.boudnik.framework.hazelcast.HazelcastTransaction;
import org.boudnik.framework.test.core.TestEntry;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class CreateSaveTest {

    @BeforeClass
    public static void beforeAll(){
        TransactionFactory.<HazelcastTransaction>getOrCreateTransaction(CacheProvider.HAZELCAST, true);
    }

    @Test
    public void testCreateSaveCommit() {
        Transaction tx = Transaction.instance();
        tx.txCommit(new TestEntry("testCreateSaveCommit"));

        Assert.assertNotNull(tx.getAndClose(TestEntry.class, "testCreateSaveCommit"));
    }

    @Test
    public void testCreateSaveRollback() {
        Transaction tx = Transaction.instance();
        new TestEntry("testCreateSaveRollback").save();
        tx.rollback();
        Assert.assertNull(tx.getAndClose(TestEntry.class, "testCreateSaveRollback"));
    }

    @Test(expected = RuntimeException.class)
    public void testCreateSaveRollbackViaException() {
        Transaction tx = Transaction.instance();
        tx.txCommit(() -> {
            new TestEntry("testCreateSaveRollback").save();
            throw new RuntimeException("Rollback Exception");
        });
        Assert.assertNull(tx.getAndClose(TestEntry.class, "testCreateSaveRollback"));
    }
}
