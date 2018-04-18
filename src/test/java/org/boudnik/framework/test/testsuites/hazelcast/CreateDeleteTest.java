package org.boudnik.framework.test.testsuites.hazelcast;

import com.hazelcast.core.Hazelcast;
import org.boudnik.framework.Transaction;
import org.boudnik.framework.test.core.TestEntry;
import org.boudnik.framework.TransactionFactory;
import org.boudnik.framework.hazelcast.HazelcastTransaction;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class CreateDeleteTest {

    private static TransactionFactory factory;
    @BeforeClass
    public static void beforeAll(){
        factory = TransactionFactory.getInstance();
        TransactionFactory.getInstance().getOrCreateHazelcastTransaction(Hazelcast::newHazelcastInstance, true);
    }

    @Test
    public void testCreateDeleteCommit() {
        HazelcastTransaction tx = TransactionFactory.getInstance().getOrCreateHazelcastTransaction();
        tx.txCommit(() -> {
            TestEntry te = new TestEntry("testCreateDeleteCommit");
            te.save();
            te.delete();
        });
        Assert.assertNull(tx.getAndClose(TestEntry.class, "testCreateDeleteCommit"));
    }

    @Test
    public void testCreateDeleteRollback() {
        HazelcastTransaction tx = TransactionFactory.getInstance().getOrCreateHazelcastTransaction();
        TestEntry te = new TestEntry("testCreateDeleteRollback");
        te.save();
        te.delete();
        tx.rollback();
        Assert.assertNull(tx.getAndClose(TestEntry.class, "testCreateDeleteRollback"));
    }

    @Test(expected = RuntimeException.class)
    public void testCreateDeleteRollbackViaException() {
        HazelcastTransaction tx = TransactionFactory.getInstance().getOrCreateHazelcastTransaction().txCommit(() -> {
            TestEntry te = new TestEntry("testCreateDeleteRollback");
            te.save();
            te.delete();
            throw new RuntimeException("Rollback Exception");
        });
        Assert.assertNull(tx.getAndClose(TestEntry.class, "testCreateDeleteRollback"));
    }
}
