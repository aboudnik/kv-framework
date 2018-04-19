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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class SaveDeleteComplexRefTest {

    private RefTestEntry ref;
    private ComplexRefTestEntry complexRef;

    @BeforeClass
    public static void beforeAll() {
        TransactionFactory.<IgniteTransaction>getOrCreateTransaction(CacheProvider.IGNITE, true).withCache(ComplexRefTestEntry.class, RefTestEntry.class, TestEntry.class);
    }

    @Test
    public void testSaveDeleteOBJCommit() {
        Transaction tx = Transaction.instance();
        tx.txCommit(() -> {
            TestEntry entry = new TestEntry("CreateSaveDeleteOBJCommit").save();
            ref = new RefTestEntry("CreateSaveDeleteOBJCommit", entry).save();
            complexRef = new ComplexRefTestEntry("CreateSaveDeleteOBJCommit", ref).save();
        });

        tx.txCommit(() -> {
            TestEntry actual = tx.get(TestEntry.class, "CreateSaveDeleteOBJCommit");
            actual.delete();
        });

        tx.txCommit(() -> {
            assertNull(tx.get(TestEntry.class, "CreateSaveDeleteOBJCommit"));
            RefTestEntry actualRef = tx.get(RefTestEntry.class, ref.getKey());
            ComplexRefTestEntry actualComplexRef = tx.get(ComplexRefTestEntry.class, complexRef.getKey());
            assertNull(actualRef.getEntry());
            assertNull(actualComplexRef.getEntry().getEntry());
            assertSame(actualComplexRef.getEntry(), actualRef);
        });
    }

    @Test
    public void testSaveDeleteRefCommit() {
        Transaction tx = Transaction.instance();
        tx.txCommit(() -> {
            TestEntry entry = new TestEntry("CreateSaveDeleteREFCommit").save();
            ref = new RefTestEntry("CreateSaveDeleteREFCommit", entry).save();
            complexRef = new ComplexRefTestEntry("CreateSaveDeleteREFCommit", ref).save();
        });

        tx.txCommit(() -> {
            RefTestEntry actualRef = tx.get(RefTestEntry.class, ref.getKey());
            actualRef.delete();
        });

        tx.txCommit(() -> {
            assertNull(tx.get(RefTestEntry.class, ref.getKey()));
            TestEntry actual = tx.get(TestEntry.class, "CreateSaveDeleteREFCommit");
            assertSame(actual, ref.getEntry());
            ComplexRefTestEntry actualComplexRef = tx.get(ComplexRefTestEntry.class, complexRef.getKey());
            assertNull(actualComplexRef.getEntry());
        });
    }

    @Test
    public void testSaveDeleteOBJRollback() {
        Transaction tx = Transaction.instance();
        tx.txCommit(() -> {
            TestEntry entry = new TestEntry("CreateSaveDeleteOBJRollback").save();
            ref = new RefTestEntry("CreateSaveDeleteOBJRollback", entry).save();
            complexRef = new ComplexRefTestEntry("CreateSaveDeleteOBJRollback", ref).save();
        });

        TestEntry entryToBeDeleted = tx.get(TestEntry.class, "CreateSaveDeleteOBJRollback");
        entryToBeDeleted.delete();
        tx.rollback();

        tx.txCommit(() -> {
            TestEntry actualEntry = tx.get(TestEntry.class, "CreateSaveDeleteOBJRollback");
            assertNotNull(actualEntry);
            RefTestEntry actualRef = tx.get(RefTestEntry.class, ref.getKey());
            ComplexRefTestEntry actualComplexRef = tx.get(ComplexRefTestEntry.class, complexRef.getKey());
            assertSame(actualRef.getEntry(), actualEntry);
            assertSame(actualComplexRef.getEntry().getEntry(), actualEntry);
            assertSame(actualComplexRef.getEntry(), actualRef);
        });
    }

    @Test
    public void testSaveDeleteRefRollback() {
        Transaction tx = Transaction.instance();
        tx.txCommit(() -> {
            TestEntry entry = new TestEntry("CreateSaveDeleteREFRollback").save();
            ref = new RefTestEntry("CreateSaveDeleteREFRollback", entry).save();
            complexRef = new ComplexRefTestEntry("CreateSaveDeleteREFRollback", ref).save();
        });

        RefTestEntry refToBeDeleted = tx.get(RefTestEntry.class, ref.getKey());
        refToBeDeleted.delete();
        tx.rollback();

        tx.txCommit(() -> {
            RefTestEntry actualRef = tx.get(RefTestEntry.class, ref.getKey());
            assertNotNull(actualRef);
            TestEntry actual = tx.get(TestEntry.class, "CreateSaveDeleteREFRollback");
            assertSame(actual, actualRef.getEntry());
            ComplexRefTestEntry actualComplexRef = tx.get(ComplexRefTestEntry.class, complexRef.getKey());
            assertSame(actualComplexRef.getEntry(), actualRef);
        });
    }

}
