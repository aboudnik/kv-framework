package org.boudnik.framework.test.testsuites;

import org.boudnik.framework.CacheProvider;
import org.boudnik.framework.TransactionFactory;
import org.boudnik.framework.hazelcast.HazelcastTransaction;
import org.boudnik.framework.ignite.IgniteTransaction;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
@Ignore("Parent class for other tests. Have no runnable tests")
public class TransactionTest {

    @Parameterized.Parameters(name = "{0} transaction")
    public static Object[] data() {
        return CacheProvider.values();
    }

    public TransactionTest(CacheProvider input, Class... classes) {
        switch (input) {
            case IGNITE: {
                TransactionFactory.<IgniteTransaction>getOrCreateTransaction(CacheProvider.IGNITE, true).withCache(classes);
                break;
            }
            case HAZELCAST: {
                TransactionFactory.<HazelcastTransaction>getOrCreateTransaction(CacheProvider.HAZELCAST, true);
                break;
            }
            default: {
                TransactionFactory.<IgniteTransaction>getOrCreateTransaction(CacheProvider.IGNITE, true).withCache(classes);
                break;
            }
        }
    }

}
