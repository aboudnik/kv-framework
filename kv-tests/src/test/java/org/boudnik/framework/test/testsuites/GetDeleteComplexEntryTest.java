package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.Context;
import org.boudnik.framework.test.core.ComplexTestEntry;
import org.boudnik.framework.test.core.ComplexTestEntry2;
import org.boudnik.framework.test.core.TestEntry;
import org.junit.Test;

import static org.junit.Assert.*;

public class GetDeleteComplexEntryTest extends TransactionTest {

    @Test
    public void testGetDeleteCommitComplexTestEntry2() {
        Context tx = Context.instance();
        ComplexTestEntry2 te = new ComplexTestEntry2(new ComplexTestEntry(new TestEntry("testGetDeleteCommitComplex")));
        tx.transaction(te::save);

        ComplexTestEntry key = te.getKey();
        tx.transaction(() -> {
            ComplexTestEntry2 entry = tx.get(ComplexTestEntry2.class, key);
            assertNotNull(entry);
            entry.delete();
        });
        tx.transaction(() -> assertNull(tx.get(ComplexTestEntry2.class, key)));
    }

    @Test
    public void testGetDeleteRollbackComplexTestEntry2() {
        Context tx = Context.instance();
        ComplexTestEntry2 te = new ComplexTestEntry2(new ComplexTestEntry(new TestEntry("testCommitDeleteRollback")));
        tx.transaction(te::save);

        ComplexTestEntry key = te.getKey();
        tx.transaction(() -> {
            ComplexTestEntry2 entry = tx.get(ComplexTestEntry2.class, key);
            assertNotNull(entry);
            entry.delete();
            tx.rollback();
        });

        tx.transaction(() -> assertNotNull(tx.get(ComplexTestEntry2.class, key)));
    }

    @Test
    public void testGetDeleteRollbackViaExceptionComplexTestEntry2() {
        Context tx = Context.instance();
        ComplexTestEntry2 te = new ComplexTestEntry2(new ComplexTestEntry(new TestEntry("testGetDeleteRollback")));
        tx.transaction(te::save);

        ComplexTestEntry key = te.getKey();
        tx.transaction(() -> assertNotNull(tx.get(ComplexTestEntry2.class, key)));
        try {
            tx.transaction(() -> {
                ComplexTestEntry2 entry = tx.get(ComplexTestEntry2.class, key);
                assertNotNull(entry);
                entry.delete();
                throw new RuntimeException("Rollback Exception");
            });
        } catch (RuntimeException e) {
            assertEquals("Rollback Exception", e.getMessage());
        } finally {
            tx.transaction(() -> assertNotNull(tx.get(ComplexTestEntry2.class, key)));
        }
    }
}
