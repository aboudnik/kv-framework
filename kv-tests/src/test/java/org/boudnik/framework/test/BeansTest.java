package org.boudnik.framework.test;

import org.boudnik.framework.util.Beans;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * @author Alexandre_Boudnik
 * @since 04/10/18 12:46
 */
public class BeansTest {

    private Beans beans = new Beans();

    @SuppressWarnings("WeakerAccess")
    public static class Bean<T> {
        private T foo;

        public T getFoo() {
            return foo;
        }

        public void setFoo(T foo) {
            this.foo = foo;
        }
    }

    private Bean<String> b1 = new Bean<>();
    private Bean<String> b2 = new Bean<>();
    private Bean<String> b3 = new Bean<>();

    @Before
    public void setUp() {
        b1.setFoo("b1");
        b2.setFoo("b2");
        b3.setFoo("b1");
    }

    @Test
    public void same() {
        assertTrue(beans.equals(b1, b3));
    }

    @Test
    public void reflection() {
        assertTrue(beans.equals(b1, b1));
    }

    @Test
    public void different() {
        assertFalse(beans.equals(b1, b2));
    }

    @Test
    public void set() {
        beans.set(b1, b2);
        assertTrue(beans.equals(b1, b2));
    }

}
