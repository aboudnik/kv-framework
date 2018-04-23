package org.boudnik.framework.test;

import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.boudnik.framework.CacheProvider;
import org.boudnik.framework.TransactionFactory;
import org.boudnik.framework.ignite.IgniteTransaction;
import org.boudnik.framework.test.core.*;
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
        Main.class,
        ObjSaveTest.class,
})
public class IgniteTransactionTestSuite {

    @BeforeClass
    public static void beforeAll() {
        TransactionFactory.getOrCreateTransaction(CacheProvider.IGNITE, () -> new IgniteTransaction(Ignition.getOrStart(new IgniteConfiguration())), true)
                .withCache(ComplexRefTestEntry.class, ComplexTestEntry.class, ComplexTestEntry2.class, MutableTestEntry.class, RefTestEntry.class, TestEntry.class);
    }
}
