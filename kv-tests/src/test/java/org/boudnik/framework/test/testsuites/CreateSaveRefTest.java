package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.Context;
import org.boudnik.framework.test.core.RefTestEntry;
import org.boudnik.framework.test.core.TestEntry;
import org.junit.Test;

import static org.junit.Assert.assertSame;

public class CreateSaveRefTest extends TransactionTest {

    private RefTestEntry ref;

    @Test
    public void testCreateSaveCommit() {
        Context tx = Context.instance();
        tx.transaction(() -> {
            TestEntry entry = new TestEntry("test").save();
            ref = new RefTestEntry("test", entry).save();
            assertSame(tx.get(TestEntry.class, "test"), entry);
            assertSame(ref.getEntry(), entry);
        });

        TestEntry actual = tx.get(TestEntry.class, "test");
        TestEntry expected = ref.getEntry();
        assertSame(actual, expected);
    }
}
