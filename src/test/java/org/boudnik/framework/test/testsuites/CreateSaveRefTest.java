package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.Transaction;
import org.boudnik.framework.test.core.RefTestEntry;
import org.boudnik.framework.test.core.TestEntry;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class CreateSaveRefTest {
    @BeforeClass
    public static void beforeAll() {
        Transaction.instance().withCache(RefTestEntry.class, TestEntry.class);
    }

    @Test
    public void testCreateSaveCommit() {
        Transaction tx = Transaction.instance();
        final RefTestEntry[] ref = new RefTestEntry[1];
        tx.txCommit(() -> {
            TestEntry entry = new TestEntry("test");
            System.out.println("entry = " + entry);
            entry.save();
            ref[0] = new RefTestEntry(1, entry);
            ref[0].save();
        });
        RefTestEntry refTestEntry = tx.get(RefTestEntry.class, ref[0].getKey());
        TestEntry test = tx.get(TestEntry.class, "test");
        Assert.assertNotNull(refTestEntry);
        Assert.assertNotNull(test);
        Assert.assertEquals(refTestEntry.getRef().get().getKey(), test.getKey());
    }
}
