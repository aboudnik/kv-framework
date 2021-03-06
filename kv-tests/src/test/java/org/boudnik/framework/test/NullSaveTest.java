package org.boudnik.framework.test;

import org.boudnik.framework.Context;
import org.boudnik.framework.test.core.TestEntry;
import org.boudnik.framework.test.testsuites.TransactionTest;
import org.junit.Test;

public class NullSaveTest extends TransactionTest {

    @Test(expected = NullPointerException.class)
    public void nullSaveTest() {
        Context tx = Context.instance();
//        tx.get(null, null);
        TestEntry nullTestEntry = new TestEntry(null);
        tx.transaction(nullTestEntry::save);
    }
}
