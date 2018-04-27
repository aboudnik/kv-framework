package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.Context;
import org.boudnik.framework.test.core.ArrayTestEntry;
import org.junit.Assert;
import org.junit.Test;

public class SaveGetArrayTest extends TransactionTest {

    @Test
    public void testCreateSaveCommit() {
        Context tx = Context.instance();
        ArrayTestEntry arrayTestEntry = new ArrayTestEntry("testCreateSaveArrayCommit1");
        arrayTestEntry.setUrl(new int[]{1, 2, 3, 4, 5});
        tx.transaction(arrayTestEntry);
        ArrayTestEntry actual = tx.get(ArrayTestEntry.class, arrayTestEntry.getKey());
        Assert.assertNotNull(actual);
        Assert.assertArrayEquals(arrayTestEntry.getUrl(), actual.getUrl());
    }

    @Test
    public void testCreateSaveRollback() {
        Context tx = Context.instance();
        ArrayTestEntry arrayTestEntry = new ArrayTestEntry("testCreateSaveArrayRollback1").save();
        Assert.assertNotNull(arrayTestEntry);
        tx.rollback();
        Assert.assertNull(tx.get(ArrayTestEntry.class, arrayTestEntry.getKey()));
    }

    @Test(expected = RuntimeException.class)
    public void testCreateSaveRollbackViaException() {
        Context tx = Context.instance();
        ArrayTestEntry arrayTestEntry = new ArrayTestEntry("testCreateSaveArrayRollback1");
        try {
            tx.transaction(() -> {
                arrayTestEntry.save();
                throw new RuntimeException("Rollback Exception");
            });
        } finally {
            Assert.assertNull(tx.get(ArrayTestEntry.class, arrayTestEntry.getKey()));
        }
    }
}
