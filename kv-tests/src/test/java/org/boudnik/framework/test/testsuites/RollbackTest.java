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
    private Person person;

    @Test
    public void testCreateSaveUpdateRollback() {

        Context context = Context.instance();
        person = new Person(SSN, "John", "Doe");
        context.transaction(() -> person.save());

        try {
            context.transaction(() -> {
                person = context.get(Person.class, SSN);
                person.fname = "Lisa";
                throw new RuntimeException();
            });
        } catch (RuntimeException ignored) {
        } finally {
            assertEquals("John", context.getAndClose(Person.class, SSN).fname);
        }
    }

    @Test
    public void testCreateSaveDeleteRollback() {

        Context context = Context.instance();
        person = new Person(SSN, "James", "Doe");
        context.transaction(() -> person.save());

        try {
            context.transaction(() -> {
                person = context.get(Person.class, SSN);
                person.delete();
                throw new RuntimeException();
            });
        } catch (RuntimeException ignored) {
        } finally {
            context.transaction(() -> assertNotNull(context.get(Person.class, SSN)));
        }
    }
}
