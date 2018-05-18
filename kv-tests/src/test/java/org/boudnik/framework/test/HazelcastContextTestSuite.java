package org.boudnik.framework.test;

import com.googlecode.junittoolbox.SuiteClasses;
import com.googlecode.junittoolbox.WildcardPatternSuite;
import org.boudnik.framework.test.testsuites.TransactionTest;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

@RunWith(WildcardPatternSuite.class)
@SuiteClasses({"testsuites/*Test.class",
        "*Test.class",
})
public class HazelcastContextTestSuite {

    @BeforeClass
    public static void beforeAll() {
        TransactionTest.setProvider("Hazelcast_3_9");
        Initializer.initHazelcast_3_9();
    }
}
