package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.Transaction;
import org.boudnik.framework.test.core.RefTestEntry;
import org.boudnik.framework.test.core.TestEntry;
import org.junit.BeforeClass;
import org.junit.Test;

public class CreateSaveRefTest {
    @BeforeClass
    public static void beforeAll() {
        Transaction.instance().withCache(RefTestEntry.class, TestEntry.class);
    }

//    @Test
//    public void testCreateSaveCommit2() {
//        Transaction tx = Transaction.instance();
//        tx.txCommit(() -> {
//            TestEntry te2 = new TestEntry("testCreateSaveCommit2");
//            TestEntry te3 = new TestEntry("testCreateSaveCommit3");
//            OBJ.Implementation impl = new ImplObject("impl");
//            OBJ.Implementation.REF ref = impl.new REF(TestEntry.class);
//            ref.set(te2);
//            System.out.println("REF: " + ref);
//            te2.save();
//            te3.save();
//        });
//
//        TestEntry te2 = tx.getAndClose(TestEntry.class, "testCreateSaveCommit2");
//        TestEntry te3 = tx.getAndClose(TestEntry.class, "testCreateSaveCommit3");
//        Assert.assertNotNull(te2);
//        Assert.assertNotNull(te3);
//        OBJ.Implementation.REF ref = new ImplObject("impl").new REF(TestEntry.class);
//        ref.set(te2);
//        System.out.println(ref.get());
//    }

/*
    @Test
    public void testCreateSaveCommit() {
        Transaction tx = Transaction.instance();
        TestEntry testEntry = new TestEntry("test");
        OBJ.Implementation<String>.REF<String, TestEntry> ref = new OBJ.Implementation<String>("test"){}.new REF<String, TestEntry> (TestEntry.class);
        ref.set(testEntry);
        RefTestEntry refTestEntry = new RefTestEntry(ref);
        refTestEntry.getKey();
        tx.txCommit(refTestEntry);

        RefTestEntry refTestEntry1 = tx.getAndClose(RefTestEntry.class, refTestEntry.getKey());

        Assert.assertNotNull(refTestEntry1);
        Assert.assertNotNull(refTestEntry1.getKey());
        System.out.println(refTestEntry.getKey()); //<-reference = testEntry
        System.out.println(refTestEntry1.getKey()); //<-reference = null
        Assert.assertEquals(refTestEntry.getKey().get(), testEntry);
        Assert.assertNotNull(refTestEntry1.getKey().get()); //<-NPE
    }
*/
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
        tx.txCommit(() -> {
            TestEntry entry = ref[0].ref.get();
            System.out.println("entry = " + entry);
        });
    }
}
