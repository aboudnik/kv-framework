package org.boudnik.framework.test.testsuites.hazelcast;

import com.hazelcast.core.Hazelcast;
import org.boudnik.framework.TransactionFactory;
import org.boudnik.framework.hazelcast.HazelcastTransaction;
import org.boudnik.framework.test.core.MutableTestEntry;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class GetDeleteTest {

    private static TransactionFactory factory;
    @BeforeClass
    public static void beforeAll(){
        factory = TransactionFactory.getInstance();
        TransactionFactory.getInstance().getOrCreateHazelcastTransaction(Hazelcast::newHazelcastInstance, true);
    }

    @Test
    public void testGetDeleteCommit() {
        HazelcastTransaction tx = factory.getOrCreateHazelcastTransaction();
        tx.txCommit(new MutableTestEntry("testGetDeleteCommit"));

        MutableTestEntry entry = tx.get(MutableTestEntry.class, "testGetDeleteCommit");
        Assert.assertNotNull(entry);

        tx.txCommit(entry::delete);
        Assert.assertNull(tx.get(MutableTestEntry.class, "testGetDeleteCommit"));
    }

    @Test
    public void testGetDeleteRollback() {
        HazelcastTransaction tx = factory.getOrCreateHazelcastTransaction();
        tx.txCommit(new MutableTestEntry("testGetDeleteRollback"));

        MutableTestEntry entry = tx.get(MutableTestEntry.class, "testGetDeleteRollback");
        Assert.assertNotNull(entry);

        entry.delete();
        tx.rollback();
        Assert.assertNotNull(tx.get(MutableTestEntry.class, "testGetDeleteRollback"));
    }

    @Test(expected = RuntimeException.class)
    public void testGetDeleteRollbackViaException() {
        HazelcastTransaction tx = factory.getOrCreateHazelcastTransaction();
        tx.txCommit(new MutableTestEntry("testGetDeleteRollback"));


        MutableTestEntry entry = tx.get(MutableTestEntry.class, "testGetDeleteRollback");

        Assert.assertNotNull(entry);
        tx.txCommit(() -> {
            entry.delete();
            throw new RuntimeException("Rollback Exception");
            });

        Assert.assertNotNull(tx.get(MutableTestEntry.class, "testGetDeleteRollback"));
    }
}
