package org.boudnik.framework.test.testsuites;

import com.hazelcast.core.Hazelcast;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.boudnik.framework.CacheProvider;
import org.boudnik.framework.Transaction;
import org.boudnik.framework.TransactionFactory;
import org.boudnik.framework.test.core.ComplexTestEntry;
import org.boudnik.framework.test.core.ComplexTestEntry2;
import org.boudnik.framework.test.core.TestEntry;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class GetUpdateSaveDeleteComplexEntryTest extends TransactionTest{

    private static final String NEW_VALUE = "New Value";

    public GetUpdateSaveDeleteComplexEntryTest(CacheProvider input) {
        super(input, ComplexTestEntry2.class);
    }

    @Test
    public void testGetUpdateSaveDeleteCommit() {
        Transaction tx = Transaction.instance();
        ComplexTestEntry2 te = new ComplexTestEntry2(new ComplexTestEntry(new TestEntry("testCommitDeleteCommit")));
        tx.txCommit(te);

        ComplexTestEntry key = te.getKey();
        ComplexTestEntry2 entry = tx.get(ComplexTestEntry2.class, key);
        Assert.assertNotNull(entry);
        Assert.assertNull(entry.getKey().getValue());
        Assert.assertNull(entry.getValue());

        tx.txCommit(() -> {
            entry.setValue(NEW_VALUE);
            entry.getKey().setValue(NEW_VALUE);
            entry.save();
            entry.delete();
        });
        ComplexTestEntry2 updatedEntry = tx.getAndClose(ComplexTestEntry2.class, key);
        Assert.assertEquals(NEW_VALUE, updatedEntry.getValue());
        Assert.assertEquals(NEW_VALUE, updatedEntry.getKey().getValue());
    }

    @Test
    public void testGetUpdateSaveDeleteRollback() {
        Transaction tx = Transaction.instance();
        ComplexTestEntry2 te = new ComplexTestEntry2(new ComplexTestEntry(new TestEntry("testCommitDeleteRollback")));
        tx.txCommit(te);

        ComplexTestEntry key = te.getKey();
        ComplexTestEntry2 entry = tx.get(ComplexTestEntry2.class, key);
        Assert.assertNotNull(entry);
        Assert.assertNull(entry.getValue());
        Assert.assertNull(entry.getKey().getValue());

        entry.setValue(NEW_VALUE);
        entry.getKey().setValue(NEW_VALUE);
        ComplexTestEntry2 saveResult = entry.save();
        Assert.assertNotNull(saveResult);
        entry.delete();
        tx.rollback();
        ComplexTestEntry2 notUpdatedEntry = tx.getAndClose(ComplexTestEntry2.class, key);
        Assert.assertNull(notUpdatedEntry.getValue());
        Assert.assertNull(notUpdatedEntry.getKey().getValue());
    }

    @Test(expected = RuntimeException.class)
    public void testGetUpdateSaveDeleteRollbackViaException() {
        Transaction tx = Transaction.instance();
        ComplexTestEntry2 te = new ComplexTestEntry2(new ComplexTestEntry(new TestEntry("testCommitDeleteRollback")));
        tx.txCommit(te);

        ComplexTestEntry key = te.getKey();
        ComplexTestEntry2 entry = tx.get(ComplexTestEntry2.class, key);
        Assert.assertNotNull(entry);

        tx.txCommit(() -> {
            entry.setValue(NEW_VALUE);
            entry.getKey().setValue(NEW_VALUE);
            entry.save();
            entry.delete();
            throw new RuntimeException("Rollback Exception");
        });
        ComplexTestEntry2 notUpdatedEntry = tx.getAndClose(ComplexTestEntry2.class, key);
        Assert.assertNull(notUpdatedEntry.getKey().getValue());
        Assert.assertNull(notUpdatedEntry.getValue());
    }
}
