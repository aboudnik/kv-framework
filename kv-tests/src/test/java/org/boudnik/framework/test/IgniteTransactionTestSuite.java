package org.boudnik.framework.test;

import com.googlecode.junittoolbox.SuiteClasses;
import com.googlecode.junittoolbox.WildcardPatternSuite;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

@RunWith(WildcardPatternSuite.class)
@SuiteClasses({"testsuites/*Test.class",
        "*Test.class",
        "Main.class"
})
public class IgniteTransactionTestSuite {

    @BeforeClass
    public static void beforeAll() {
        Initializer.initIgnite();
    }
}
