package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.Context;
import org.boudnik.framework.test.core.RefTestEntry;
import org.boudnik.framework.test.core.TestEntry;
import org.junit.Test;

import static org.junit.Assert.*;

public class SaveDeleteRefTest extends TransactionTest  {

    private RefTestEntry ref;

    @Test
    public void testSaveDeleteCommit() {
        Context tx = Context.instance();
        tx.transaction(() -> {
            TestEntry entry = new TestEntry("CreateSaveDeleteCommitRef").save();
            ref = new RefTestEntry("CreateSaveDeleteCommitRef", entry).save();
        });

        tx.transaction(() -> {
            TestEntry actual = tx.get(TestEntry.class, "CreateSaveDeleteCommitRef");
            actual.delete();
        });

        assertNull(tx.get(TestEntry.class, "CreateSaveDeleteCommitRef"));
        RefTestEntry actualRef = tx.get(RefTestEntry.class, ref.getKey());
        assertNull(actualRef.getEntry());
    }

    @Test
    public void testSaveDeleteRollback() {
        Context tx = Context.instance();
        tx.transaction(() -> {
            TestEntry entry = new TestEntry("CreateSaveDeleteRollbackRef").save();
            ref = new RefTestEntry("CreateSaveDeleteRollbackRef", entry).save();
        });

        TestEntry actual = tx.get(TestEntry.class, "CreateSaveDeleteRollbackRef");
        actual.delete();
        tx.rollback();

        TestEntry actualEntry = tx.get(TestEntry.class, "CreateSaveDeleteRollbackRef");
        assertNotNull(actualEntry);
        RefTestEntry actualRef = tx.get(RefTestEntry.class, ref.getKey());
        assertNotNull(actualRef);
        assertSame(actualRef.getEntry(), actualEntry);
    }
}
