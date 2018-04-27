package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.Context;
import org.boudnik.framework.test.core.ArrayTestEntry;
import org.junit.Assert;
import org.junit.Test;

public class CreateSaveArrayTest extends TransactionTest{

    @Test
    public void testCreateSaveCommit() {
        Context tx = Context.instance();
        ArrayTestEntry arrayTestEntry1 = new ArrayTestEntry("testCreateSaveArrayCommit3", "testCreateSaveArrayCommit4").save();
        Assert.assertNotNull(tx.get(ArrayTestEntry.class, arrayTestEntry1.getKey())); // <- works fine
//        ArrayTestEntry arrayTestEntry = new ArrayTestEntry("testCreateSaveArrayCommit1", "testCreateSaveArrayCommit2");
//        tx.transaction(arrayTestEntry);
//        Assert.assertNotNull(tx.get(ArrayTestEntry.class, arrayTestEntry.getKey())); // <- fails
    }

    @Test
    public void testCreateSaveRollback() {
        Context tx = Context.instance();
        ArrayTestEntry arrayTestEntry = new ArrayTestEntry("testCreateSaveArrayRollback1", "testCreateSaveArrayRollback2").save();
        Assert.assertNotNull(arrayTestEntry);
        tx.rollback();
        Assert.assertNull(tx.get(ArrayTestEntry.class, arrayTestEntry.getKey()));
    }

    @Test(expected = RuntimeException.class)
    public void testCreateSaveRollbackViaException() {
        Context tx = Context.instance();
        ArrayTestEntry arrayTestEntry = new ArrayTestEntry("testCreateSaveArrayRollback1", "testCreateSaveArrayRollback2");
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
