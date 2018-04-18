package org.boudnik.framework.test.testsuites.hazelcast;

import com.hazelcast.core.Hazelcast;
import org.boudnik.framework.Transaction;
import org.boudnik.framework.TransactionFactory;
import org.boudnik.framework.hazelcast.HazelcastTransaction;
import org.boudnik.framework.test.core.TestEntry;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class CreateSaveTest {

    private static TransactionFactory factory;
    @BeforeClass
    public static void beforeAll(){
        factory = TransactionFactory.getInstance();
        TransactionFactory.getInstance().getOrCreateHazelcastTransaction(Hazelcast::newHazelcastInstance, true);
    }

    @Test
    public void testCreateSaveCommit() {
        HazelcastTransaction tx = factory.getOrCreateHazelcastTransaction();
        tx.txCommit(new TestEntry("testCreateSaveCommit"));

        Assert.assertNotNull(tx.getAndClose(TestEntry.class, "testCreateSaveCommit"));
    }

    @Test
    public void testCreateSaveRollback() {
        HazelcastTransaction tx = factory.getOrCreateHazelcastTransaction();
        new TestEntry("testCreateSaveRollback").save();
        tx.rollback();
        Assert.assertNull(tx.getAndClose(TestEntry.class, "testCreateSaveRollback"));
    }

    @Test(expected = RuntimeException.class)
    public void testCreateSaveRollbackViaException() {
        HazelcastTransaction tx = factory.getOrCreateHazelcastTransaction();
        tx.txCommit(() -> {
            new TestEntry("testCreateSaveRollback").save();
            throw new RuntimeException("Rollback Exception");
        });
        Assert.assertNull(tx.getAndClose(TestEntry.class, "testCreateSaveRollback"));
    }
}
