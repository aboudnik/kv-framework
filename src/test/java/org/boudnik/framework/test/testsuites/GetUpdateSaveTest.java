package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.CacheProvider;
import org.boudnik.framework.Transaction;
import org.boudnik.framework.test.core.MutableTestEntry;
import org.junit.Assert;
import org.junit.Test;

public class GetUpdateSaveTest extends TransactionTest {

    private static final String NEW_VALUE = "New Value";

    public GetUpdateSaveTest(CacheProvider input) {
        super(input, MutableTestEntry.class);
    }

    @Test
    public void testGetUpdateSaveCommit() {
        Transaction tx = Transaction.instance();
        tx.txCommit(new MutableTestEntry("testGetUpdateSaveCommit"));

        final MutableTestEntry entry = tx.get(MutableTestEntry.class, "testGetUpdateSaveCommit");
        Assert.assertNotNull(entry);
        Assert.assertNull(entry.getValue());

        tx.txCommit(() -> {
            entry.setValue(NEW_VALUE);
            entry.save();
        });
        Assert.assertEquals(NEW_VALUE, tx.get(MutableTestEntry.class, "testGetUpdateSaveCommit").getValue());
    }

    @Test
    public void testGetUpdateSaveRollback() {
        Transaction tx = Transaction.instance();
        tx.txCommit(new MutableTestEntry("testGetUpdateSaveRollback"));

        final MutableTestEntry entry = tx.get(MutableTestEntry.class, "testGetUpdateSaveRollback");
        Assert.assertNotNull(entry);
        Assert.assertNull(entry.getValue());

        entry.setValue(NEW_VALUE);
        MutableTestEntry saveResult = entry.save();
        Assert.assertNotNull(saveResult);
        tx.rollback();
        Assert.assertNull(tx.get(MutableTestEntry.class, "testGetUpdateSaveRollback").getValue());
    }

    @Test(expected = RuntimeException.class)
    public void testGetUpdateSaveRollbackViaException() {
        Transaction tx = Transaction.instance();
        tx.txCommit(new MutableTestEntry("testGetUpdateSaveRollback"));

        final MutableTestEntry entry = tx.get(MutableTestEntry.class, "testGetUpdateSaveRollback");
        Assert.assertNotNull(entry);
        Assert.assertNull(entry.getValue());

        tx.txCommit(() -> {
            entry.setValue(NEW_VALUE);
            entry.save();
            throw new RuntimeException("Rollback Exception");
        });
        Assert.assertNull(tx.get(MutableTestEntry.class, "testGetUpdateSaveRollback").getValue());
    }
}
