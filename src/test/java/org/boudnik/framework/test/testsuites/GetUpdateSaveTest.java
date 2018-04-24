package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.Context;
import org.boudnik.framework.test.core.MutableTestEntry;
import org.junit.Assert;
import org.junit.Test;

public class GetUpdateSaveTest extends TransactionTest {

    private static final String NEW_VALUE = "New Value";

    @Test
    public void testGetUpdateSaveCommit() {
        Context tx = Context.instance();
        tx.transaction(new MutableTestEntry("testGetUpdateSaveCommit"));

        final MutableTestEntry entry = tx.get(MutableTestEntry.class, "testGetUpdateSaveCommit");
        Assert.assertNotNull(entry);
        Assert.assertNull(entry.getValue());

        tx.transaction(() -> {
            entry.setValue(NEW_VALUE);
            entry.save();
        });
        Assert.assertEquals(NEW_VALUE, tx.get(MutableTestEntry.class, "testGetUpdateSaveCommit").getValue());
    }

    @Test
    public void testGetUpdateSaveRollback() {
        Context tx = Context.instance();
        tx.transaction(new MutableTestEntry("testGetUpdateSaveRollback"));

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
        Context tx = Context.instance();
        tx.transaction(new MutableTestEntry("testGetUpdateSaveRollback"));

        final MutableTestEntry entry = tx.get(MutableTestEntry.class, "testGetUpdateSaveRollback");
        Assert.assertNotNull(entry);
        Assert.assertNull(entry.getValue());
        try {
            tx.transaction(() -> {
                entry.setValue(NEW_VALUE);
                entry.save();
                throw new RuntimeException("Rollback Exception");
            });
        } finally {
            Assert.assertNull(tx.get(MutableTestEntry.class, "testGetUpdateSaveRollback").getValue());
        }
    }
}
