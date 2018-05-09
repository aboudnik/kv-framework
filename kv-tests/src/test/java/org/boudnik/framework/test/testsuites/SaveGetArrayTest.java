package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.Context;
import org.boudnik.framework.test.core.ArrayTestEntry;
import org.junit.Test;

import static org.junit.Assert.*;

public class SaveGetArrayTest extends TransactionTest {

    @Test
    public void testCreateSaveCommit() {
        Context tx = Context.instance();
        ArrayTestEntry arrayTestEntry = new ArrayTestEntry("testCreateSaveArrayCommit1");
        arrayTestEntry.setUrl(new int[]{1, 2, 3, 4, 5});
        tx.transaction(arrayTestEntry::save);
        tx.transaction(() -> {
            ArrayTestEntry actual = tx.get(ArrayTestEntry.class, arrayTestEntry.getKey());
            assertNotNull(actual);
            assertArrayEquals(arrayTestEntry.getUrl(), actual.getUrl());
        });
    }

    @Test
    public void testCreateSaveRollback() {
        Context tx = Context.instance();
        tx.transaction(() -> {
            ArrayTestEntry arrayTestEntry = new ArrayTestEntry("testCreateSaveArrayRollback1").save();
            assertNotNull(arrayTestEntry);
            tx.rollback();
        });
        tx.transaction(() -> {
            ArrayTestEntry arrayTestEntry = new ArrayTestEntry("testCreateSaveArrayRollback1");
            assertNull(tx.get(ArrayTestEntry.class, arrayTestEntry.getKey()));
        });
    }

    @Test
    public void testCreateSaveRollbackViaException() {
        Context tx = Context.instance();
        ArrayTestEntry arrayTestEntry = new ArrayTestEntry("testCreateSaveArrayRollback1");
        try {
            tx.transaction(() -> {
                arrayTestEntry.save();
                throw new RuntimeException("Rollback Exception");
            });
        } catch (RuntimeException e) {
            assertEquals("Rollback Exception", e.getMessage());
        } finally {
            tx.transaction(() -> assertNull(tx.get(ArrayTestEntry.class, arrayTestEntry.getKey())));
        }
    }
}
