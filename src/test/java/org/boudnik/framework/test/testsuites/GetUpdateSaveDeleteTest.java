package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.Context;
import org.boudnik.framework.test.core.MutableTestEntry;
import org.junit.Assert;
import org.junit.Test;

public class GetUpdateSaveDeleteTest extends TransactionTest {

    private static final String NEW_VALUE = "New Value";

    @Test
    public void testGetUpdateSaveDeleteCommit() {
        Context tx = Context.instance();
        tx.transaction(new MutableTestEntry("testGetUpdateSaveDeleteCommit"));

        final MutableTestEntry entry = tx.get(MutableTestEntry.class, "testGetUpdateSaveDeleteCommit");
        Assert.assertNotNull(entry);
        Assert.assertNull(entry.getValue());

        tx.transaction(() -> {
            entry.setValue(NEW_VALUE);
            entry.save();
            entry.delete();
        });
        Assert.assertNull(tx.get(MutableTestEntry.class, "testGetUpdateSaveDeleteCommit"));
    }

    @Test
    public void testGetUpdateSaveDeleteRollback() {
        Context tx = Context.instance();
        tx.transaction(new MutableTestEntry("testGetUpdateSaveDeleteRollback"));

        MutableTestEntry entry = tx.get(MutableTestEntry.class, "testGetUpdateSaveDeleteRollback");
        Assert.assertNotNull(entry);
        Assert.assertNull(entry.getValue());

        entry.setValue(NEW_VALUE);
        MutableTestEntry saveResult = entry.save();
        Assert.assertNotNull(saveResult);
        entry.delete();
        tx.rollback();
        entry = tx.get(MutableTestEntry.class, "testGetUpdateSaveDeleteRollback");
        Assert.assertNotNull(entry);
        Assert.assertNull(entry.getValue());
    }

    @Test(expected = RuntimeException.class)
    public void testGetUpdateSaveDeleteRollbackViaException() {
        Context tx = Context.instance();
        tx.transaction(new MutableTestEntry("testGetUpdateSaveDeleteRollback"));

        final MutableTestEntry entry = tx.get(MutableTestEntry.class, "testGetUpdateSaveDeleteRollback");
        Assert.assertNotNull(entry);
        Assert.assertNull(entry.getValue());
        try {
            tx.transaction(() -> {
                entry.setValue(NEW_VALUE);
                entry.save();
                entry.delete();
                throw new RuntimeException("Rollback Exception");
            });
        } finally {
            MutableTestEntry updatedEntry = tx.get(MutableTestEntry.class, "testGetUpdateSaveDeleteRollback");
            Assert.assertNotNull(updatedEntry);
            Assert.assertNull(updatedEntry.getValue());
        }
    }
}
