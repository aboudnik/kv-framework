package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.Transaction;
import org.boudnik.framework.test.core.MutableTestEntry;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class GetUpdateSaveDeleteTest {

    private static final String NEW_VALUE = "New Value";

    @BeforeClass
    public static void beforeAll(){
        Transaction.instance().withCache(MutableTestEntry.class);
    }

    @Test
    public void testGetUpdateSaveDeleteCommit() {
        Transaction tx = Transaction.instance();
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

        Transaction tx = Transaction.instance();
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

        Transaction tx = Transaction.instance();
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
