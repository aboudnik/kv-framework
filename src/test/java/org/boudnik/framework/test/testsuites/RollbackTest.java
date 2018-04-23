package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.Context;
import org.boudnik.framework.pocs.Person;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Sergey Nuyanzin
 * @since 4/23/2018
 */
public class RollbackTest extends TransactionTest {

    private static final String SSN = "601-77-1234";
    private Person person;

    @Test(expected = RuntimeException.class)
    public void testCreateSaveUpdateRollback() {

        Context context = Context.instance();
        person = new Person(SSN, "John", "Doe");
        context.transaction(() -> person.save());

        try {
            context.transaction(() -> {
                person = context.get(Person.class, SSN);
                person.fname = "Lisa";
                person.save();
                throw new RuntimeException();
            });
        } finally {
            assertEquals("John", person.fname);
        }
    }

    @Test(expected = RuntimeException.class)
    public void testCreateSaveDeleteRollback() {

        Context context = Context.instance();
        person = new Person(SSN, "John", "Doe");
        context.transaction(() -> person.save());

        try {
            context.transaction(() -> {
                person = context.get(Person.class, SSN);
                person.delete();
                throw new RuntimeException();
            });
        } finally {
            assertEquals("John", person.fname);
        }
    }
}
