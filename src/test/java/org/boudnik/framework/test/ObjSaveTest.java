package org.boudnik.framework.test;

import org.boudnik.framework.Transaction;
import org.boudnik.framework.test.core.TestEntry;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ObjSaveTest {

    @BeforeClass
    public static void beforeAll(){
        Transaction.instance().withCache(TestEntry.class);
    }

    @Test
    public void checkSeveralEntriesWithDifferentKeys() {
        Transaction tx = Transaction.instance();
        tx.txCommit(() -> {
            new TestEntry("http://localhost/1").save("checkSeveralEntriesWithDifferentKeys");
            new TestEntry("http://localhost/1").save();
        });

        TestEntry entry = tx.get(TestEntry.class, "http://localhost/1");
        Assert.assertNotNull(entry);
        entry = tx.get(TestEntry.class, "checkSeveralEntriesWithDifferentKeys");
        Assert.assertNotNull(entry);
    }
}
