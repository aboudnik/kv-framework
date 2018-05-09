package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.Context;
import org.boudnik.framework.test.core.RefTestEntry;
import org.boudnik.framework.test.core.TestEntry;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

public class SaveGetRefTest extends TransactionTest {

    @Ignore
    @Test
    public void testSaveGetCommit() {
        Context tx = Context.instance();
        TestEntry entry = new TestEntry("SaveGetCommitRef");
        RefTestEntry ref = new RefTestEntry("SaveGetCommitRef", entry);
        tx.transaction(() -> {
            entry.save();
            ref.save();
            assertSame(tx.get(TestEntry.class, "SaveGetCommitRef"), entry);
            assertSame(ref.getEntry(), entry);
        });

        tx.transaction(() -> {
            TestEntry actual = tx.get(TestEntry.class, "SaveGetCommitRef");
            TestEntry expected = ref.getEntry();
            assertSame(actual, expected);
            RefTestEntry actualRef = tx.get(RefTestEntry.class, ref.getKey());
            assertNotNull(actualRef);
            assertSame(actualRef.getEntry(), ref.getEntry());
        });
    }

    @Ignore
    @Test
    public void testSaveGetRollback() {
        Context tx = Context.instance();
        tx.transaction(() -> {
            TestEntry entry = new TestEntry("SaveGetRollbackRef").save();
            new RefTestEntry("SaveGetRollbackRef", entry).save();
            tx.rollback();
        });
        tx.transaction(() -> {
            TestEntry entry = new TestEntry("SaveGetRollbackRef").save();
            RefTestEntry ref = new RefTestEntry("SaveGetRollbackRef", entry).save();
            assertNull(tx.get(TestEntry.class, "SaveGetRollbackRef"));
            assertNull(tx.get(RefTestEntry.class, ref.getKey()));
        });
    }
}
