package org.boudnik.framework.test;

import com.hazelcast.core.Hazelcast;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.boudnik.framework.CacheProvider;
import org.boudnik.framework.TransactionFactory;
import org.boudnik.framework.hazelcast.HazelcastTransaction;
import org.boudnik.framework.ignite.IgniteTransaction;
import org.boudnik.framework.test.core.*;

public class Initializer {
    private static final Class[] classes = {ComplexRefTestEntry.class, ComplexTestEntry.class, ComplexTestEntry2.class, MutableTestEntry.class, RefTestEntry.class, TestEntry.class};

    public static void initIgnite() {
        TransactionFactory.getOrCreateTransaction(CacheProvider.IGNITE, () -> new IgniteTransaction(Ignition.getOrStart(new IgniteConfiguration())), true)
                .withCache(classes);
    }

    public static void initHazelcast() {
        TransactionFactory.getOrCreateTransaction(CacheProvider.HAZELCAST, () -> new HazelcastTransaction(Hazelcast.newHazelcastInstance()), true);
    }
}
