package org.boudnik.framework.test;

import org.boudnik.framework.Context;
import org.boudnik.framework.OBJ;
import org.boudnik.framework.test.core.TestEntry;
import org.boudnik.framework.test.testsuites.TransactionTest;
import org.junit.Assert;
import org.junit.Test;

public class ObjSaveTest extends TransactionTest {

    @Test
    public void checkSeveralEntriesWithDifferentKeys() {
        Context tx = Context.instance();
        tx.transaction(() -> new TestEntry("http://localhost/1").save());

        TestEntry entry = tx.get(TestEntry.class, "http://localhost/1");
        Assert.assertNotNull(entry);
    }
}
