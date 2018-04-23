package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.Context;
import org.boudnik.framework.test.core.ComplexTestEntry;
import org.boudnik.framework.test.core.ComplexTestEntry2;
import org.boudnik.framework.test.core.TestEntry;
import org.junit.Assert;
import org.junit.Test;

public class MultipleClassesWithSameKeyTest extends TransactionTest  {

    @Test
    public void MultipleEntriesWithSameKey() {
        Context tx = Context.instance();
        ComplexTestEntry2 te = new ComplexTestEntry2(new ComplexTestEntry(new TestEntry("Some key")));
        tx.transaction(te);
        tx.transaction(new TestEntry("Some key"));

        ComplexTestEntry key = te.getKey();
        ComplexTestEntry2 entry = tx.get(ComplexTestEntry2.class, key);
        Assert.assertNotNull(entry);
        tx.transaction(entry::delete);
        Assert.assertNull(tx.getAndClose(ComplexTestEntry2.class, key));
        Assert.assertNotNull(tx.getAndClose(TestEntry.class, "Some key"));
    }

    @Test
    public void MultipleEntriesWithSameKey2() {
        Context tx = Context.instance();
        ComplexTestEntry2 te = new ComplexTestEntry2(new ComplexTestEntry(new TestEntry("Other key")));
        tx.transaction(te);
        tx.transaction(new TestEntry("Other key"));

        ComplexTestEntry key = te.getKey();
        TestEntry entry = tx.get(TestEntry.class, "Other key");
        Assert.assertNotNull(entry);
        tx.transaction(entry::delete);
        Assert.assertNull(tx.getAndClose(TestEntry.class, "Other key"));
        Assert.assertNotNull(tx.getAndClose(ComplexTestEntry2.class, key));
    }
}
