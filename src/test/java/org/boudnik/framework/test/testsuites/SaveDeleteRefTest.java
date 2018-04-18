package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.Transaction;
import org.boudnik.framework.test.core.RefTestEntry;
import org.boudnik.framework.test.core.TestEntry;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertNull;

public class SaveDeleteRefTest {

    private RefTestEntry ref;

    @BeforeClass
    public static void beforeAll() {
        Transaction.instance().withCache(RefTestEntry.class, TestEntry.class);
    }

    @Test
    public void testSaveDelete() {
        Transaction tx = Transaction.instance();
        tx.txCommit(() -> {
            TestEntry entry = new TestEntry("CreateSaveDeleteRef").save();
            ref = new RefTestEntry(1, entry).save();
        });

        tx.txCommit(() -> {
            TestEntry actual = tx.get(TestEntry.class, "CreateSaveDeleteRef");
            actual.delete();
        });

        tx.txCommit(() -> {
            assertNull(tx.get(TestEntry.class, "CreateSaveDeleteRef"));
            RefTestEntry actualRef = tx.get(RefTestEntry.class, ref.getKey());
            assertNull(actualRef.getEntry());
        });
    }
}
