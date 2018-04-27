package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.Context;
import org.boudnik.framework.test.core.ArrayTestEntry;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class SaveDeleteArrayTest extends TransactionTest {

    @Test
    public void testSaveDeleteCommit() {
        Context tx = Context.instance();
        ArrayTestEntry arrayTestEntry = new ArrayTestEntry("CreateSaveDeleteCommitArray1", "CreateSaveDeleteCommitArray2");
        tx.transaction(arrayTestEntry);

        tx.transaction(() -> {
            ArrayTestEntry actual = tx.get(ArrayTestEntry.class, arrayTestEntry.getKey());
            actual.delete();
        });

        assertNull(tx.get(ArrayTestEntry.class, "CreateSaveDeleteCommitRef"));
    }

    @Test
    public void testSaveDeleteRollback() {
        Context tx = Context.instance();
        ArrayTestEntry arrayTestEntry = new ArrayTestEntry("CreateSaveDeleteCommitArray1", "CreateSaveDeleteCommitArray2");
        tx.transaction(arrayTestEntry);


        ArrayTestEntry actual = tx.get(ArrayTestEntry.class, arrayTestEntry.getKey());
        actual.delete();
        tx.rollback();

        ArrayTestEntry actualEntry = tx.get(ArrayTestEntry.class, arrayTestEntry.getKey());
        assertNotNull(actualEntry);
    }
}

