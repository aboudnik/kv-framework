package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.Transaction;
import org.boudnik.framework.test.core.ComplexRefTestEntry;
import org.boudnik.framework.test.core.RefTestEntry;
import org.boudnik.framework.test.core.TestEntry;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class SaveDeleteComplexRefTest {

    private RefTestEntry ref;
    private ComplexRefTestEntry complexRef;

    @BeforeClass
    public static void beforeAll() {
        Transaction.instance().withCache(ComplexRefTestEntry.class, RefTestEntry.class, TestEntry.class);
    }

    @Test
    public void testSaveDeleteOBJ() {
        Transaction tx = Transaction.instance();
        tx.txCommit(() -> {
            TestEntry entry = new TestEntry("CreateSaveDeleteComplexRef").save();
            ref = new RefTestEntry(1, entry).save();
            complexRef = new ComplexRefTestEntry(1, ref).save();
        });

        tx.txCommit(() -> {
            TestEntry actual = tx.get(TestEntry.class, "CreateSaveDeleteComplexRef");
            actual.delete();
        });

        tx.txCommit(() -> {
            assertNull(tx.get(TestEntry.class, "CreateSaveDeleteComplexRef"));
            RefTestEntry actualRef = tx.get(RefTestEntry.class, ref.getKey());
            ComplexRefTestEntry actualComplexRef = tx.get(ComplexRefTestEntry.class, complexRef.getKey());
            assertNull(actualRef.getEntry());
            assertNull(actualComplexRef.getEntry().getEntry());
            assertSame(actualComplexRef.getEntry(), actualRef);
        });
    }
    @Test
    public void testSaveDeleteRef() {
        Transaction tx = Transaction.instance();
        tx.txCommit(() -> {
            TestEntry entry = new TestEntry("CreateSaveDeleteComplexRef").save();
            ref = new RefTestEntry(1, entry).save();
            complexRef = new ComplexRefTestEntry(1, ref).save();
        });

        tx.txCommit(() -> {
            RefTestEntry actualRef = tx.get(RefTestEntry.class, ref.getKey());
            actualRef.delete();
        });

        tx.txCommit(() -> {
            assertNull(tx.get(RefTestEntry.class, ref.getKey()));
            TestEntry actual = tx.get(TestEntry.class, "CreateSaveDeleteComplexRef");
            assertSame(actual, ref.getEntry());
            ComplexRefTestEntry actualComplexRef = tx.get(ComplexRefTestEntry.class, complexRef.getKey());
            assertNull(actualComplexRef.getEntry());
        });
    }
}
