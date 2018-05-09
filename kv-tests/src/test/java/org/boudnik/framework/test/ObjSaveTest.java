package org.boudnik.framework.test;

import org.boudnik.framework.Context;
import org.boudnik.framework.test.core.TestEntry;
import org.boudnik.framework.test.testsuites.TransactionTest;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class ObjSaveTest extends TransactionTest {

    @Test
    public void checkSeveralEntriesWithDifferentKeys() {
        Context tx = Context.instance();
        tx.transaction(() -> new TestEntry("http://localhost/1").save());

        tx.transaction(() -> assertNotNull(tx.get(TestEntry.class, "http://localhost/1")));
    }
}
