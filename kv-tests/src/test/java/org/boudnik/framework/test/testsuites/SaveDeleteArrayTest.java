package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.Context;
import org.boudnik.framework.test.core.ArrayTestEntry;
import org.junit.Test;

import static org.junit.Assert.*;

public class SaveDeleteArrayTest extends TransactionTest {

    @Test
    public void testSaveDeleteCommit() {
        Context tx = Context.instance();
        ArrayTestEntry arrayTestEntry = new ArrayTestEntry("CreateSaveDeleteCommitArray1");
        tx.transaction(arrayTestEntry::save);

        tx.transaction(() -> {
            ArrayTestEntry actual = tx.get(ArrayTestEntry.class, arrayTestEntry.getKey());
            assertNotNull(actual);
            actual.delete();
        });

        tx.transaction(() -> assertNull(tx.get(ArrayTestEntry.class, "CreateSaveDeleteCommitRef")));
    }

    @Test
    public void testSaveDeleteRollback() {
        Context tx = Context.instance();
        ArrayTestEntry arrayTestEntry = new ArrayTestEntry("CreateSaveDeleteCommitArray1");
        arrayTestEntry.setUrl(new int[]{1, 2, 3, 4, 5});
        tx.transaction(arrayTestEntry::save);


        tx.transaction(() -> {
            ArrayTestEntry actual = tx.get(ArrayTestEntry.class, arrayTestEntry.getKey());
            assertNotNull(actual);
            actual.delete();
            tx.rollback();
        });

        tx.transaction(() -> {
            ArrayTestEntry actualEntry = tx.get(ArrayTestEntry.class, arrayTestEntry.getKey());
            assertNotNull(actualEntry);
            assertArrayEquals(arrayTestEntry.getUrl(), actualEntry.getUrl());
        });
    }
}

