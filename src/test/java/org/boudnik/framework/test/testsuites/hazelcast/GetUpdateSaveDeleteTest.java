package org.boudnik.framework.test.testsuites.hazelcast;

import com.hazelcast.core.Hazelcast;
import org.boudnik.framework.TransactionFactory;
import org.boudnik.framework.hazelcast.HazelcastTransaction;
import org.boudnik.framework.test.core.MutableTestEntry;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class GetUpdateSaveDeleteTest {

    private static final String NEW_VALUE = "New Value";
    private static TransactionFactory factory;
    @BeforeClass
    public static void beforeAll(){
        factory = TransactionFactory.getInstance();
        TransactionFactory.getInstance().getOrCreateHazelcastTransaction(Hazelcast::newHazelcastInstance, true);
    }

    @Test
    public void testGetUpdateSaveDeleteCommit() {
        HazelcastTransaction tx = factory.getOrCreateHazelcastTransaction();
        tx.txCommit(new MutableTestEntry("testGetUpdateSaveDeleteCommit"));

        final MutableTestEntry entry = tx.get(MutableTestEntry.class, "testGetUpdateSaveDeleteCommit");
        Assert.assertNotNull(entry);
        Assert.assertNull(entry.getValue());

        tx.txCommit(() -> {
                    entry.setValue(NEW_VALUE);
                    entry.save();
                    entry.delete();
                });
        Assert.assertNull(tx.get(MutableTestEntry.class, "testGetUpdateSaveDeleteCommit"));
    }

    @Test
    public void testGetUpdateSaveDeleteRollback() {

        HazelcastTransaction tx = factory.getOrCreateHazelcastTransaction();
        tx.txCommit(new MutableTestEntry("testGetUpdateSaveDeleteRollback"));

        MutableTestEntry entry = tx.get(MutableTestEntry.class, "testGetUpdateSaveDeleteRollback");
        Assert.assertNotNull(entry);
        Assert.assertNull(entry.getValue());

        entry.setValue(NEW_VALUE);
        entry.save();
        entry.delete();
        tx.rollback();
        entry = tx.get(MutableTestEntry.class, "testGetUpdateSaveDeleteRollback");
        Assert.assertNotNull(entry);
        Assert.assertNull(entry.getValue());
    }

    @Test(expected = RuntimeException.class)
    public void testGetUpdateSaveDeleteRollbackViaException() {

        HazelcastTransaction tx = factory.getOrCreateHazelcastTransaction();
        tx.txCommit(new MutableTestEntry("testGetUpdateSaveDeleteRollback"));

        final MutableTestEntry entry = tx.get(MutableTestEntry.class, "testGetUpdateSaveDeleteRollback");
        Assert.assertNotNull(entry);
        Assert.assertNull(entry.getValue());

        tx.txCommit(() -> {
                    entry.setValue(NEW_VALUE);
                    entry.save();
                    entry.delete();
                    throw new RuntimeException("Rollback Exception");
                });
        MutableTestEntry updatedEntry = tx.get(MutableTestEntry.class, "testGetUpdateSaveDeleteRollback");
        Assert.assertNotNull(updatedEntry);
        Assert.assertNull(updatedEntry.getValue());
    }
}
