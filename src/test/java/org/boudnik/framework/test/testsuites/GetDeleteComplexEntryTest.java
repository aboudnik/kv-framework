package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.Transaction;
import org.boudnik.framework.test.core.ComplexTestEntry;
import org.boudnik.framework.test.core.ComplexTestEntry2;
import org.boudnik.framework.test.core.TestEntry;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class GetDeleteComplexEntryTest {

    @BeforeClass
    public static void beforeAll(){
        Transaction.instance().withCache(ComplexTestEntry2.class);
    }

    @Test
    public void testGetDeleteCommitComplexTestEntry2() {
        Transaction tx = Transaction.instance();
        ComplexTestEntry2 te = new ComplexTestEntry2(new ComplexTestEntry(new TestEntry("testCommitDeleteCommit")));
        tx.txCommit(te);

        ComplexTestEntry key = te.getKey();
        ComplexTestEntry2 entry = tx.get(ComplexTestEntry2.class, key);
        Assert.assertNotNull(entry);

        tx.txCommit(entry::delete);
        Assert.assertNull(tx.getAndClose(ComplexTestEntry2.class, key));
    }

    @Test
    public void testGetDeleteRollbackComplexTestEntry2() {
        Transaction tx = Transaction.instance();
        ComplexTestEntry2 te = new ComplexTestEntry2(new ComplexTestEntry(new TestEntry("testCommitDeleteRollback")));
        tx.txCommit(te);

        ComplexTestEntry key = te.getKey();
        ComplexTestEntry2 entry = tx.get(ComplexTestEntry2.class, key);
        Assert.assertNotNull(entry);

        entry.delete();
        tx.rollback();
        Assert.assertNotNull(tx.getAndClose(ComplexTestEntry2.class, key));
    }

    @Test(expected = RuntimeException.class)
    public void testGetDeleteRollbackViaExceptionComplexTestEntry2() {
        Transaction tx = Transaction.instance();
        ComplexTestEntry2 te = new ComplexTestEntry2(new ComplexTestEntry(new TestEntry("testCommitDeleteRollback")));
        tx.txCommit(te);

        ComplexTestEntry key = te.getKey();
        ComplexTestEntry2 entry = tx.get(ComplexTestEntry2.class, key);
        Assert.assertNotNull(entry);

        tx.txCommit(() -> {
            entry.delete();
            throw  new RuntimeException("RollbackException");
        });
        Assert.assertNotNull(tx.getAndClose(ComplexTestEntry2.class, key));
    }
}
