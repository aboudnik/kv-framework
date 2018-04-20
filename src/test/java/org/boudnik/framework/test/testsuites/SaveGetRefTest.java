package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.CacheProvider;
import org.boudnik.framework.Context;
import org.boudnik.framework.test.core.RefTestEntry;
import org.boudnik.framework.test.core.TestEntry;
import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class SaveGetRefTest extends TransactionTest {

    public SaveGetRefTest(CacheProvider input) {
        super(input, RefTestEntry.class, TestEntry.class);
    }

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
            assertSame(actualRef.getEntry(), ref.getEntry());
        });
    }

    @Test
    public void testSaveGetRollback() {
        Context tx = Context.instance();
        TestEntry entry = new TestEntry("SaveGetRollbackRef").save();
        RefTestEntry ref = new RefTestEntry("SaveGetRollbackRef", entry).save();

        tx.rollback();

        assertNull(tx.get(TestEntry.class, "SaveGetRollbackRef"));
        assertNull(tx.get(RefTestEntry.class, ref.getKey()));
    }
}
