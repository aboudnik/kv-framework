package org.boudnik.framework.test.testsuites;

import com.hazelcast.core.Hazelcast;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
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
                TransactionFactory.getOrCreateTransaction(CacheProvider.IGNITE, () -> new IgniteTransaction(Ignition.getOrStart(new IgniteConfiguration())), true).withCache(classes);
                break;
            }
            case HAZELCAST: {
                TransactionFactory.getOrCreateTransaction(CacheProvider.HAZELCAST, () -> new HazelcastTransaction(Hazelcast.newHazelcastInstance()), true);
                break;
            }
            default: {
                TransactionFactory.getOrCreateTransaction(CacheProvider.IGNITE, () -> new IgniteTransaction(Ignition.getOrStart(new IgniteConfiguration())), true).withCache(classes);
                break;
            }
        }
    }

}
