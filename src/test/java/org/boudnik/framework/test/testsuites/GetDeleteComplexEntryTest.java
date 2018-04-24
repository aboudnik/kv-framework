package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.Context;
import org.boudnik.framework.test.core.ComplexTestEntry;
import org.boudnik.framework.test.core.ComplexTestEntry2;
import org.boudnik.framework.test.core.TestEntry;
import org.junit.Assert;
import org.junit.Test;

public class GetDeleteComplexEntryTest extends TransactionTest {

    @Test
    public void testGetDeleteCommitComplexTestEntry2() {
        Context tx = Context.instance();
        ComplexTestEntry2 te = new ComplexTestEntry2(new ComplexTestEntry(new TestEntry("testGetDeleteCommitComplex")));
        tx.transaction(te);

        ComplexTestEntry key = te.getKey();
        ComplexTestEntry2 entry = tx.get(ComplexTestEntry2.class, key);
        Assert.assertNotNull(entry);
        tx.transaction(entry::delete);
        Assert.assertNull(tx.getAndClose(ComplexTestEntry2.class, key));
    }

    @Test
    public void testGetDeleteRollbackComplexTestEntry2() {
        Context tx = Context.instance();
        ComplexTestEntry2 te = new ComplexTestEntry2(new ComplexTestEntry(new TestEntry("testCommitDeleteRollback")));
        tx.transaction(te);

        ComplexTestEntry key = te.getKey();
        ComplexTestEntry2 entry = tx.get(ComplexTestEntry2.class, key);
        Assert.assertNotNull(entry);

        entry.delete();
        tx.rollback();
        Assert.assertNotNull(tx.getAndClose(ComplexTestEntry2.class, key));
    }

    @Test(expected = RuntimeException.class)
    public void testGetDeleteRollbackViaExceptionComplexTestEntry2() {
        Context tx = Context.instance();
        ComplexTestEntry2 te = new ComplexTestEntry2(new ComplexTestEntry(new TestEntry("testCommitDeleteRollback")));
        tx.transaction(te);

        ComplexTestEntry key = te.getKey();
        ComplexTestEntry2 entry = tx.get(ComplexTestEntry2.class, key);
        Assert.assertNotNull(entry);
        try {
            tx.transaction(() -> {
                entry.delete();
                throw new RuntimeException("Rollback Exception");
            });
        } finally {
            Assert.assertNotNull(tx.getAndClose(ComplexTestEntry2.class, key));
        }
    }
}
