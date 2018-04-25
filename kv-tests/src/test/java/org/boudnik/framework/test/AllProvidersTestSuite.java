package org.boudnik.framework.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        IgniteTransactionTestSuite.class,
        HazelcastTransactionTestSuite.class,
})
public class AllProvidersTestSuite {
}
