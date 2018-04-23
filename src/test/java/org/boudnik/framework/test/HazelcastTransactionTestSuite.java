package org.boudnik.framework.test;

import com.hazelcast.core.Hazelcast;
import org.boudnik.framework.CacheProvider;
import org.boudnik.framework.TransactionFactory;
import org.boudnik.framework.hazelcast.HazelcastTransaction;
import org.boudnik.framework.test.testsuites.*;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        CreateDeleteTest.class,
        CreateSaveDeleteTest.class,
        CreateSaveRefTest.class,
        CreateSaveTest.class,
        GetDeleteComplexEntryTest.class,
        GetDeleteTest.class,
        GetUpdateSaveComplexEntryTest.class,
        GetUpdateSaveDeleteComplexEntryTest.class,
        GetUpdateSaveDeleteTest.class,
        GetUpdateSaveTest.class,
        MultipleClassesWithSameKeyTest.class,
        SaveDeleteComplexRefTest.class,
        SaveDeleteRefTest.class,
        SaveGetComplexRefTest.class,
        SaveGetRefTest.class,
        ObjSaveTest.class,
})
public class HazelcastTransactionTestSuite {

    @BeforeClass
    public static void beforeAll() {
        TransactionFactory.getOrCreateTransaction(CacheProvider.HAZELCAST, () -> new HazelcastTransaction(Hazelcast.newHazelcastInstance()), true);
    }
}
