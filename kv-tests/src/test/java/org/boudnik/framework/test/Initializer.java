package org.boudnik.framework.test;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.boudnik.framework.TransactionFactory;
import org.boudnik.framework.h2.H2Context;
import org.boudnik.framework.hazelcast.HazelcastContext;
import org.boudnik.framework.ignite.IgniteContext;
import org.boudnik.framework.test.core.*;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

import static org.boudnik.framework.test.testsuites.TransactionTest.setProvider;

public class Initializer {
    private static final Class[] classes = {ComplexRefTestEntry.class, ComplexTestEntry.class, ComplexTestEntry2.class,
            MutableTestEntry.class, RefTestEntry.class, TestEntry.class, Person.class, ArrayTestEntry.class};

    private static final String INIT_NAME = "Test";

    public static IgniteContext initIgnite() {
        setProvider("Ignite");
        return TransactionFactory.getOrCreateTransaction(IgniteContext.class,
                () -> new IgniteContext(Ignition.getOrStart(new IgniteConfiguration().setIgniteInstanceName(INIT_NAME))), true)
                .withCache(classes);
    }

    public static HazelcastContext initHazelcast() {
        setProvider("Hazelcast");
        return TransactionFactory.getOrCreateTransaction(HazelcastContext.class,
                () -> new HazelcastContext(Hazelcast.getOrCreateHazelcastInstance(new Config(INIT_NAME))),
                true).withCache(classes);
    }

    public static H2Context initH2() {
        try {
            Class.forName("org.h2.Driver");
            Connection connection =
                    DriverManager.getConnection("jdbc:h2:mem:" + INIT_NAME + ";DB_CLOSE_DELAY=-1", "", "");
            Statement statement = connection.createStatement();
            Arrays.stream(classes).forEach(aClass -> {
                try {
                    statement.addBatch(createTable(aClass));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            statement.executeBatch();
            setProvider("H2");
            return TransactionFactory.getOrCreateTransaction(H2Context.class,
                    () -> new H2Context(connection), true)
                    .withCache(classes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private static String createTable(Class aClass) {
        String tableName = aClass.getSimpleName();
        return "CREATE TABLE IF NOT EXISTS " + tableName +
                " (" +
                "  key CHAR NOT NULL, " +
                "  value BLOB " +
                "); " +
                "CREATE UNIQUE INDEX IF NOT EXISTS " + tableName + "_key_uindex ON " + tableName + " (key);";
    }
}
