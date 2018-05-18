package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.test.Initializer;
import org.boudnik.framework.test.core.*;
import org.junit.BeforeClass;

public class TransactionTest {
    private static String provider;

    public static String getProvider() {
        return provider;
    }

    public static void setProvider(String provider) {
        TransactionTest.provider = provider;
    }

    private static final Class[] classes = {ComplexRefTestEntry.class, ComplexTestEntry.class, ComplexTestEntry2.class, MutableTestEntry.class, RefTestEntry.class, TestEntry.class, Person.class, ArrayTestEntry.class};

    @BeforeClass
    public static void beforeAll() {
        if (getProvider() == null) {
            Initializer.initIgnite();
        }
    }
}
