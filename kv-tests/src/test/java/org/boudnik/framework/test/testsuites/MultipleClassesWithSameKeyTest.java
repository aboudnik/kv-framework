package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.Context;
import org.boudnik.framework.test.core.ComplexTestEntry;
import org.boudnik.framework.test.core.ComplexTestEntry2;
import org.boudnik.framework.test.core.TestEntry;
import org.junit.Test;

import static org.junit.Assert.*;

public class MultipleClassesWithSameKeyTest extends TransactionTest {

    @Test
    public void MultipleEntriesWithSameKey() {
        Context tx = Context.instance();
        ComplexTestEntry2 te = new ComplexTestEntry2(new ComplexTestEntry(new TestEntry("Some key")));
        tx.transaction(te::save);
        tx.transaction(new TestEntry("Some key")::save);

        ComplexTestEntry key = te.getKey();
        tx.transaction(() -> {
            ComplexTestEntry2 entry = tx.get(ComplexTestEntry2.class, key);
            assertNotNull(entry);
            entry.delete();
        });
        tx.transaction(() -> {
            assertNull(tx.get(ComplexTestEntry2.class, key));
            assertNotNull(tx.get(TestEntry.class, "Some key"));
        });
    }

    @Test
    public void MultipleEntriesWithSameKey2() {
        Context tx = Context.instance();
        ComplexTestEntry2 te = new ComplexTestEntry2(new ComplexTestEntry(new TestEntry("Other key")));
        tx.transaction(te::save);
        tx.transaction(new TestEntry("Other key")::save);

        ComplexTestEntry key = te.getKey();
        tx.transaction(() -> {
            TestEntry entry = tx.get(TestEntry.class, "Other key");
            assertNotNull(entry);
            entry.delete();
        });
        tx.transaction(() -> {
            assertNull(tx.get(TestEntry.class, "Other key"));
            assertNotNull(tx.get(ComplexTestEntry2.class, key));
        });
    }
}
