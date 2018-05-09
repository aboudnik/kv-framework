package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.Context;
import org.boudnik.framework.test.core.ComplexRefTestEntry;
import org.boudnik.framework.test.core.RefTestEntry;
import org.boudnik.framework.test.core.TestEntry;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

public class SaveGetComplexRefTest extends TransactionTest {

    @Ignore
    @Test
    public void testSaveGetCommit() {
        Context tx = Context.instance();
        TestEntry entry = new TestEntry("SaveGetCommitComplexRef");
        RefTestEntry ref = new RefTestEntry("SaveGetCommitComplexRef", entry);
        ComplexRefTestEntry complexRef = new ComplexRefTestEntry("SaveGetCommitComplexRef", ref);
        tx.transaction(() -> {
            entry.save();
            ref.save();
            complexRef.save();
            assertSame(tx.get(TestEntry.class, "SaveGetCommitComplexRef"), entry);
            assertSame(ref.getEntry(), entry);
            assertSame(complexRef.getEntry(), ref);
        });

        tx.transaction(() -> {
            TestEntry actual = tx.get(TestEntry.class, "SaveGetCommitComplexRef");
            TestEntry expected = ref.getEntry();
            RefTestEntry actualRef = tx.get(RefTestEntry.class, ref.getKey());
            ComplexRefTestEntry actualComplexRef = tx.get(ComplexRefTestEntry.class, complexRef.getKey());
            assertSame(actual, expected);
            assertNotNull(actualRef);
            assertSame(actualRef.getEntry(), ref.getEntry());
            assertNotNull(actualComplexRef);
            assertSame(actualComplexRef.getEntry().getEntry(), ref.getEntry());
        });
    }

    @Ignore
    @Test
    public void testSaveGetRollback() {
        Context tx = Context.instance();
        tx.transaction(() -> {
            TestEntry entry = new TestEntry("SaveGetRollbackComplexRef").save();
            RefTestEntry ref = new RefTestEntry("SaveGetRollbackComplexRef", entry).save();
            ComplexRefTestEntry complexRef = new ComplexRefTestEntry("SaveGetRollbackComplexRef", ref).save();
            assertSame(tx.get(TestEntry.class, "SaveGetRollbackComplexRef"), entry);
            assertSame(ref.getEntry(), entry);
            assertSame(complexRef.getEntry(), ref);
            tx.rollback();
        });

        tx.transaction(() -> {
            TestEntry entry = new TestEntry("SaveGetRollbackComplexRef").save();
            RefTestEntry ref = new RefTestEntry("SaveGetRollbackComplexRef", entry).save();
            ComplexRefTestEntry complexRef = new ComplexRefTestEntry("SaveGetRollbackComplexRef", ref).save();
            assertNull(tx.get(TestEntry.class, "SaveGetRollbackComplexRef"));
            assertNull(tx.get(RefTestEntry.class, ref.getKey()));
            assertNull(tx.get(ComplexRefTestEntry.class, complexRef.getKey()));
            assertNull(ref.getEntry());
            assertNull(complexRef.getEntry());
        });
    }
}
