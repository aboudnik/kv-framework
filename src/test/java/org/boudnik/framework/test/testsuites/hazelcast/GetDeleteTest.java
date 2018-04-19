package org.boudnik.framework.test.testsuites.hazelcast;

import org.boudnik.framework.CacheProvider;
import org.boudnik.framework.Transaction;
import org.boudnik.framework.TransactionFactory;
import org.boudnik.framework.hazelcast.HazelcastTransaction;
import org.boudnik.framework.test.core.MutableTestEntry;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class GetDeleteTest {

    @BeforeClass
    public static void beforeAll(){
        TransactionFactory.<HazelcastTransaction>getOrCreateTransaction(CacheProvider.HAZELCAST, true);
    }

    @Test
    public void testGetDeleteCommit() {
        Transaction tx = Transaction.instance();
        tx.txCommit(new MutableTestEntry("testGetDeleteCommit"));

        MutableTestEntry entry = tx.get(MutableTestEntry.class, "testGetDeleteCommit");
        Assert.assertNotNull(entry);

        tx.txCommit(entry::delete);
        Assert.assertNull(tx.get(MutableTestEntry.class, "testGetDeleteCommit"));
    }

    @Test
    public void testGetDeleteRollback() {
        Transaction tx = Transaction.instance();
        tx.txCommit(new MutableTestEntry("testGetDeleteRollback"));

        MutableTestEntry entry = tx.get(MutableTestEntry.class, "testGetDeleteRollback");
        Assert.assertNotNull(entry);

        entry.delete();
        tx.rollback();
        Assert.assertNotNull(tx.get(MutableTestEntry.class, "testGetDeleteRollback"));
    }

    @Test(expected = RuntimeException.class)
    public void testGetDeleteRollbackViaException() {
        Transaction tx = Transaction.instance();
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
