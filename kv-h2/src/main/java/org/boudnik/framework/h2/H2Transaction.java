package org.boudnik.framework.h2;

import org.boudnik.framework.Context;
import org.boudnik.framework.OBJ;
import org.boudnik.framework.util.Beans;

import javax.cache.Cache;
import java.io.ObjectInputStream;
import java.sql.*;

public class H2Transaction extends Context {

    private final Connection connection;

    public H2Transaction(Connection connection) {
        this.connection = connection;
    }

    @Override
    protected <K, V extends OBJ<K>> V toObject(Object external, K identity) throws Exception {
        //noinspection unchecked
        V v = Beans.clone(meta, (V) external);
        v.setKey(identity);
        return v;
    }

    @Override
    protected <K> Object getExternal(Class<? extends OBJ> clazz, K identity) throws Exception {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT value FROM " + clazz.getSimpleName() + " WHERE key=?")) {
            preparedStatement.setObject(1, identity, Types.JAVA_OBJECT);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next())
                    try (ObjectInputStream ois = new ObjectInputStream(resultSet.getBinaryStream(1))) {
                        //noinspection unchecked
                        return ois.readObject();
                    }
                else
                    return null;
            }
        }
    }

    @Override
    protected <K, V extends OBJ<K>> Cache<K, V> cache(Class<? extends OBJ> clazz) {
        return new H2Cache<>(connection, clazz);
    }

    @Override
    protected void startTransactionIfNotStarted() {
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected boolean isTransactionExist() {
        try {
            return !connection.getAutoCommit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void engineSpecificCommitAction() {
        try {
            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void engineSpecificRollbackAction() {
        try {
            connection.rollback();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void engineSpecificClearAction() {

    }
}
