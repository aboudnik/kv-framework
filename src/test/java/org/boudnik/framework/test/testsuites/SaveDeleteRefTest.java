package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.CacheProvider;
import org.boudnik.framework.Transaction;
import org.boudnik.framework.test.core.RefTestEntry;
import org.boudnik.framework.test.core.TestEntry;
import org.junit.Test;

import static org.junit.Assert.*;

public class SaveDeleteRefTest extends TransactionTest {

    private RefTestEntry ref;

    public SaveDeleteRefTest(CacheProvider input) {
        super(input, RefTestEntry.class, TestEntry.class);
    }

    @Test
    public void testSaveDeleteCommit() {
        Transaction tx = Transaction.instance();
        tx.txCommit(() -> {
            TestEntry entry = new TestEntry("CreateSaveDeleteCommitRef").save();
            ref = new RefTestEntry("CreateSaveDeleteCommitRef", entry).save();
        });

        tx.txCommit(() -> {
            TestEntry actual = tx.get(TestEntry.class, "CreateSaveDeleteCommitRef");
            actual.delete();
        });


        assertNull(tx.get(TestEntry.class, "CreateSaveDeleteCommitRef"));
        RefTestEntry actualRef = tx.get(RefTestEntry.class, ref.getKey());
        assertNull(actualRef.getEntry());
    }

    @Test
    public void testSaveDeleteRollback() {
        Transaction tx = Transaction.instance();
        tx.txCommit(() -> {
            TestEntry entry = new TestEntry("CreateSaveDeleteRollbackRef").save();
            ref = new RefTestEntry("CreateSaveDeleteRollbackRef", entry).save();
        });

        TestEntry actual = tx.get(TestEntry.class, "CreateSaveDeleteRollbackRef");
        actual.delete();
        tx.rollback();

        tx.txCommit(() -> {
            TestEntry actualEntry = tx.get(TestEntry.class, "CreateSaveDeleteRollbackRef");
            assertNotNull(actualEntry);
            RefTestEntry actualRef = tx.get(RefTestEntry.class, ref.getKey());
            assertNotNull(actualRef);
            assertSame(actualRef.getEntry(), actualEntry);
        });
    }
}
