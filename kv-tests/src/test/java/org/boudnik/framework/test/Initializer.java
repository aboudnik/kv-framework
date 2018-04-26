package org.boudnik.framework.test;

import com.hazelcast.core.Hazelcast;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.boudnik.framework.TransactionFactory;
import org.boudnik.framework.h2.H2Transaction;
import org.boudnik.framework.hazelcast.HazelcastTransaction;
import org.boudnik.framework.ignite.IgniteTransaction;
import org.boudnik.framework.test.core.*;

import java.sql.*;
import java.util.Arrays;

public class Initializer {
    private static final Class[] classes = {ComplexRefTestEntry.class, ComplexTestEntry.class, ComplexTestEntry2.class, MutableTestEntry.class, RefTestEntry.class, TestEntry.class, Person.class};

    public static void initIgnite() {
        TransactionFactory.getOrCreateTransaction(IgniteTransaction.class, () -> new IgniteTransaction(Ignition.getOrStart(new IgniteConfiguration())), true)
                .withCache(classes);
    }

    public static void initHazelcast() {
        TransactionFactory.getOrCreateTransaction(HazelcastTransaction.class,
                () -> new HazelcastTransaction(Hazelcast.newHazelcastInstance()),
                true);
    }

    public static void initH2() {
        try {
            Class.forName("org.h2.Driver");
            final Connection conn = DriverManager.getConnection(
                    "jdbc:h2:tcp://localhost:1521/default",
                    "",
                    ""
            );

            final Statement statement = conn.createStatement();
            Arrays.stream(classes).forEach(aClass -> {
                try {
                    statement.addBatch(
                            "CREATE TABLE IF NOT EXISTS " + aClass.getSimpleName() + " (" +
                                    "  key BLOB, " +
                                    "  value BLOB " +
                                    ");");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });

            statement.executeBatch();

            TransactionFactory.getOrCreateTransaction(
                    H2Transaction.class,
                    () -> new H2Transaction(conn),
                    true);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
