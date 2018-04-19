package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.CacheProvider;
import org.boudnik.framework.Transaction;
import org.boudnik.framework.test.core.ComplexTestEntry;
import org.boudnik.framework.test.core.ComplexTestEntry2;
import org.boudnik.framework.test.core.TestEntry;
import org.junit.Assert;
import org.junit.Test;

public class MultipleClassesWithSameKeyTest extends TransactionTest {
    public MultipleClassesWithSameKeyTest(CacheProvider input) {
        super(input, TestEntry.class, ComplexTestEntry2.class);
    }

    @Test
    public void MultipleEntriesWithSameKey() {
        Transaction tx = Transaction.instance();
        ComplexTestEntry2 te = new ComplexTestEntry2(new ComplexTestEntry(new TestEntry("Some key")));
        tx.txCommit(te);
        tx.txCommit(new TestEntry("Some key"));

        ComplexTestEntry key = te.getKey();
        ComplexTestEntry2 entry = tx.get(ComplexTestEntry2.class, key);
        Assert.assertNotNull(entry);
        tx.txCommit(entry::delete);
        Assert.assertNull(tx.getAndClose(ComplexTestEntry2.class, key));
        Assert.assertNotNull(tx.getAndClose(TestEntry.class, "Some key"));
    }

    @Test
    public void MultipleEntriesWithSameKey2() {
        Transaction tx = Transaction.instance();
        ComplexTestEntry2 te = new ComplexTestEntry2(new ComplexTestEntry(new TestEntry("Other key")));
        tx.txCommit(te);
        tx.txCommit(new TestEntry("Other key"));

        ComplexTestEntry key = te.getKey();
        TestEntry entry = tx.get(TestEntry.class, "Other key");
        Assert.assertNotNull(entry);
        tx.txCommit(entry::delete);
        Assert.assertNull(tx.getAndClose(TestEntry.class, "Other key"));
        Assert.assertNotNull(tx.getAndClose(ComplexTestEntry2.class, key));
    }
}
