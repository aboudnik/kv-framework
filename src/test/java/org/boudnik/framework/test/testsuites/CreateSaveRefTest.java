package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.OBJ;
import org.boudnik.framework.Transaction;
import org.boudnik.framework.test.core.RefTestEntry;
import org.boudnik.framework.test.core.TestEntry;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


public class CreateSaveRefTest {

    @BeforeClass
    public static void beforeAll() {
        Transaction.instance().withCacheName(RefTestEntry.class);
    }

    @Test
    public void testCreateSaveCommit() {
        Transaction tx = Transaction.instance();
        TestEntry testEntry = new TestEntry("test");
        OBJ.Implementation<String>.REF<String, TestEntry> ref = new OBJ.Implementation<String>("test"){}.new REF<String, TestEntry> (TestEntry.class);
        ref.set(testEntry);
        RefTestEntry refTestEntry = new RefTestEntry(ref);
        tx.txCommit(refTestEntry);

        RefTestEntry refTestEntry1 = tx.getAndClose(RefTestEntry.class, refTestEntry.getKey());

        Assert.assertNotNull(refTestEntry1);
        Assert.assertNotNull(refTestEntry1.getKey());
        System.out.println(refTestEntry.getKey()); //<-reference = testEntry
        System.out.println(refTestEntry1.getKey()); //<-reference = null
        Assert.assertEquals(refTestEntry.getKey().get(), testEntry);
        Assert.assertNotNull(refTestEntry1.getKey().get()); //<-NPE
    }
}
