package org.boudnik.framework.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        IgniteContextTestSuite.class,
        HazelcastContextTestSuite.class,
        H2ContextTestSuite.class,
})
public class AllProvidersTestSuite {
}
