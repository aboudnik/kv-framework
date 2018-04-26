package org.boudnik.framework.test.testsuites;

import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.boudnik.framework.TransactionFactory;
import org.boudnik.framework.ignite.IgniteTransaction;
import org.boudnik.framework.test.core.*;
import org.junit.BeforeClass;

public class TransactionTest {

    private static final Class[] classes = {ComplexRefTestEntry.class, ComplexTestEntry.class, ComplexTestEntry2.class, MutableTestEntry.class, RefTestEntry.class, TestEntry.class, Person.class};
    @BeforeClass
    public static void beforeAll() {
        if (TransactionFactory.getCurrentTransaction() == null) {
            TransactionFactory.getOrCreateTransaction(IgniteTransaction.class, () -> new IgniteTransaction(Ignition.getOrStart(new IgniteConfiguration())), true)
                    .withCache(classes);
        }
    }
}
