package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.Context;
import org.boudnik.framework.test.core.Person;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Sergey Nuyanzin
 * @since 4/23/2018
 */
public class RollbackTest extends TransactionTest {

    private static final String SSN = "601-77-1234";

    @Test
    public void testCreateSaveUpdateRollback() {

        Context context = Context.instance();

        context.transaction(() -> new Person(SSN, "John", "Doe").save());

        try {
            context.transaction(() -> {
                Person person = context.get(Person.class, SSN);
                assertNotNull(person);
                person.fname = "Lisa";
                throw new RuntimeException("Rollback Exception");
            });
        } catch (RuntimeException e) {
            assertEquals("Rollback Exception", e.getMessage());
        } finally {
            context.transaction(() -> {
                Person person = context.get(Person.class, SSN);
                assertNotNull(person);
                assertEquals("John", person.fname);
            });
        }
    }

    @Test
    public void testCreateSaveDeleteRollback() {

        Context context = Context.instance();
        context.transaction(() -> new Person(SSN, "James", "Doe").save());

        try {
            context.transaction(() -> {
                Person person = context.get(Person.class, SSN);
                assertNotNull(person);
                person.delete();
                throw new RuntimeException("Rollback Exception");
            });
        } catch (RuntimeException e) {
            assertEquals("Rollback Exception", e.getMessage());
        } finally {
            context.transaction(() -> assertNotNull(context.get(Person.class, SSN)));
        }
    }
}
