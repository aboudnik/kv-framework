package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.TransactionFactory;
import org.boudnik.framework.test.Initializer;
import org.junit.BeforeClass;

public class TransactionTest {

    @BeforeClass
    public static void beforeAll() {
        if (TransactionFactory.getCurrentTransaction() == null) {
            Initializer.initIgnite();
        }
    }
}
