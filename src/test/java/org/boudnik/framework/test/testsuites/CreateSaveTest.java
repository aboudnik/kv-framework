package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.CacheProvider;
import org.boudnik.framework.Context;
import org.boudnik.framework.test.core.TestEntry;
import org.junit.Assert;
import org.junit.Test;

public class CreateSaveTest extends TransactionTest {

    public CreateSaveTest(CacheProvider input) {
        super(input, TestEntry.class);
    }

    @Test
    public void testCreateSaveCommit() {
        Context tx = Context.instance();
        tx.transaction(new TestEntry("testCreateSaveCommit"));
        Assert.assertNotNull(tx.getAndClose(TestEntry.class, "testCreateSaveCommit"));
    }

    @Test
    public void testCreateSaveRollback() {
        Context tx = Context.instance();
        TestEntry saveResult = new TestEntry("testCreateSaveRollback").save();
        Assert.assertNotNull(saveResult);
        tx.rollback();
        Assert.assertNull(tx.getAndClose(TestEntry.class, "testCreateSaveRollback"));
    }

    @Test(expected = RuntimeException.class)
    public void testCreateSaveRollbackViaException() {
        Context tx = Context.instance();
        tx.transaction(() -> {
            new TestEntry("testCreateSaveRollback").save();
            throw new RuntimeException("Rollback Exception");
        });
        Assert.assertNull(tx.getAndClose(TestEntry.class, "testCreateSaveRollback"));
    }
}
