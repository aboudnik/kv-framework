package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.CacheProvider;
import org.boudnik.framework.Transaction;
import org.boudnik.framework.TransactionFactory;
import org.boudnik.framework.ignite.IgniteTransaction;
import org.boudnik.framework.test.core.ComplexRefTestEntry;
import org.boudnik.framework.test.core.RefTestEntry;
import org.boudnik.framework.test.core.TestEntry;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class SaveGetComplexRefTest {

    @BeforeClass
    public static void beforeAll() {
        TransactionFactory.<IgniteTransaction>getOrCreateTransaction(CacheProvider.IGNITE, true).withCache(ComplexRefTestEntry.class, RefTestEntry.class, TestEntry.class);
    }

    @Test
    public void testSaveGetCommit() {
        Transaction tx = Transaction.instance();
        TestEntry entry = new TestEntry("SaveGetCommitComplexRef");
        RefTestEntry ref = new RefTestEntry("SaveGetCommitComplexRef", entry);
        ComplexRefTestEntry complexRef = new ComplexRefTestEntry("SaveGetCommitComplexRef", ref);
        tx.txCommit(() -> {
            entry.save();
            ref.save();
            complexRef.save();
            assertSame(tx.get(TestEntry.class, "SaveGetCommitComplexRef"), entry);
            assertSame(ref.getEntry(), entry);
            assertSame(complexRef.getEntry(), ref);
        });

        tx.txCommit(() -> {
            TestEntry actual = tx.get(TestEntry.class, "SaveGetCommitComplexRef");
            TestEntry expected = ref.getEntry();
            RefTestEntry actualRef = tx.get(RefTestEntry.class, ref.getKey());
            ComplexRefTestEntry actualComplexRef = tx.get(ComplexRefTestEntry.class, complexRef.getKey());
            assertSame(actual, expected);
            assertSame(actualRef.getEntry(), ref.getEntry());
            assertSame(actualComplexRef.getEntry().getEntry(), ref.getEntry());
        });
    }

    @Test
    public void testSaveGetRollback() {
        Transaction tx = Transaction.instance();
        TestEntry entry = new TestEntry("SaveGetRollbackComplexRef").save();
        RefTestEntry ref = new RefTestEntry("SaveGetRollbackComplexRef", entry).save();
        ComplexRefTestEntry complexRef = new ComplexRefTestEntry("SaveGetRollbackComplexRef", ref).save();
        assertSame(tx.get(TestEntry.class, "SaveGetRollbackComplexRef"), entry);
        assertSame(ref.getEntry(), entry);
        assertSame(complexRef.getEntry(), ref);
        tx.rollback();

        assertNull(tx.get(TestEntry.class, "SaveGetRollbackComplexRef"));
        assertNull(tx.get(RefTestEntry.class, ref.getKey()));
        assertNull(tx.get(ComplexRefTestEntry.class, complexRef.getKey()));
        assertNull(ref.getEntry());
        assertNull(complexRef.getEntry());
    }
}
