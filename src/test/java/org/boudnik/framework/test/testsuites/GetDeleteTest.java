package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.CacheProvider;
import org.boudnik.framework.Context;
import org.boudnik.framework.test.core.MutableTestEntry;
import org.junit.Assert;
import org.junit.Test;

public class GetDeleteTest extends TransactionTest {

    public GetDeleteTest(CacheProvider input) {
        super(input, MutableTestEntry.class);
    }

    @Test
    public void testGetDeleteCommit() {
        Context tx = Context.instance();
        tx.transaction(new MutableTestEntry("testGetDeleteCommit"));

        MutableTestEntry entry = tx.get(MutableTestEntry.class, "testGetDeleteCommit");
        Assert.assertNotNull(entry);

        tx.transaction(entry::delete);
        Assert.assertNull(tx.get(MutableTestEntry.class, "testGetDeleteCommit"));
    }

    @Test
    public void testGetDeleteRollback() {
        Context tx = Context.instance();
        tx.transaction(new MutableTestEntry("testGetDeleteRollback"));

        MutableTestEntry entry = tx.get(MutableTestEntry.class, "testGetDeleteRollback");
        Assert.assertNotNull(entry);

        entry.delete();
        tx.rollback();
        Assert.assertNotNull(tx.get(MutableTestEntry.class, "testGetDeleteRollback"));
    }

    @Test(expected = RuntimeException.class)
    public void testGetDeleteRollbackViaException() {
        Context tx = Context.instance();
        tx.transaction(new MutableTestEntry("testGetDeleteRollback"));

        MutableTestEntry entry = tx.get(MutableTestEntry.class, "testGetDeleteRollback");
        Assert.assertNotNull(entry);
        tx.transaction(() -> {
            entry.delete();
            throw new RuntimeException("Rollback Exception");
        });

        Assert.assertNotNull(tx.get(MutableTestEntry.class, "testGetDeleteRollback"));
    }
}
