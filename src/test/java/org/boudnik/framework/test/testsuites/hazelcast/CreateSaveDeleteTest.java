package org.boudnik.framework.test.testsuites.hazelcast;

import com.hazelcast.core.Hazelcast;
import org.boudnik.framework.Transaction;
import org.boudnik.framework.TransactionFactory;
import org.boudnik.framework.hazelcast.HazelcastTransaction;
import org.boudnik.framework.test.core.TestEntry;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class CreateSaveDeleteTest {

    private static TransactionFactory factory;
    @BeforeClass
    public static void beforeAll(){
        factory = TransactionFactory.getInstance();
        TransactionFactory.getInstance().getOrCreateHazelcastTransaction(Hazelcast::newHazelcastInstance, true);
    }

    @Test
    public void testCommitDeleteCommit() {
        HazelcastTransaction tx = factory.getOrCreateHazelcastTransaction();
        tx.txCommit(new TestEntry("testCommitDeleteCommit"));

        TestEntry entry = tx.get(TestEntry.class, "testCommitDeleteCommit");
        Assert.assertNotNull(entry);

        tx.txCommit(entry::delete);
        Assert.assertNull(tx.getAndClose(TestEntry.class, "testCommitDeleteCommit"));
    }

    @Test
    public void testCommitDeleteRollback() {
        HazelcastTransaction tx = factory.getOrCreateHazelcastTransaction();
        tx.txCommit(new TestEntry("testCommitDeleteRollback"));

        TestEntry entry = tx.get(TestEntry.class, "testCommitDeleteRollback");
        System.out.println();
        Assert.assertNotNull(entry);

        entry.delete();
        tx.rollback();
        Assert.assertNotNull(tx.getAndClose(TestEntry.class, "testCommitDeleteRollback"));
    }

    @Test(expected = RuntimeException.class)
    public void testCommitDeleteRollbackViaException() {
        HazelcastTransaction tx = factory.getOrCreateHazelcastTransaction();
        tx.txCommit(new TestEntry("testCommitDeleteRollback"));

        TestEntry entry = tx.get(TestEntry.class, "testCommitDeleteRollback");
        Assert.assertNotNull(entry);

        tx.txCommit(() -> {
            entry.delete();
            throw new RuntimeException("RollbackException");
        });
        Assert.assertNotNull(tx.getAndClose(TestEntry.class, "testCommitDeleteRollback"));
    }
}
