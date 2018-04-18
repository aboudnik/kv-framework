package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.Transaction;
import org.boudnik.framework.test.core.ComplexRefTestEntry;
import org.boudnik.framework.test.core.RefTestEntry;
import org.boudnik.framework.test.core.TestEntry;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertSame;

public class SaveGetComplexRefTest {

    private RefTestEntry ref;
    private ComplexRefTestEntry complexRef;

    @BeforeClass
    public static void beforeAll() {
        Transaction.instance().withCache(ComplexRefTestEntry.class, RefTestEntry.class, TestEntry.class);
    }

    @Test
    public void testSaveGetCommit() {
        Transaction tx = Transaction.instance();
        tx.txCommit(() -> {
            TestEntry entry = new TestEntry("test").save();
            ref = new RefTestEntry(1, entry).save();
            complexRef = new ComplexRefTestEntry(1, ref).save();
            assertSame(tx.get(TestEntry.class, "test"), entry);
            assertSame(ref.getEntry(), entry);
            assertSame(complexRef.getEntry(), ref);
        });

        tx.txCommit(() -> {
            TestEntry actual = tx.get(TestEntry.class, "test");
            TestEntry expected = ref.getEntry();
            assertSame(actual, expected);
            RefTestEntry actualRef = tx.get(RefTestEntry.class, ref.getKey());
            assertSame(actualRef.getEntry(), ref.getEntry());
            ComplexRefTestEntry actualComplexRef = tx.get(ComplexRefTestEntry.class, complexRef.getKey());
            assertSame(actualComplexRef.getEntry().getEntry(), ref.getEntry());
        });
    }
}
