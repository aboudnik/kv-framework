package org.boudnik.framework.h2;

import org.boudnik.framework.Context;
import org.boudnik.framework.OBJ;

import javax.cache.Cache;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.sql.*;

public class H2Context extends Context {

    private final Connection connection;

    public H2Context(Connection connection) {
        this.connection = connection;
    }

    @Override
    protected <K> Object getNative(Class<? extends OBJ> clazz, K identity) throws Exception {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT value FROM " + clazz.getSimpleName() + " WHERE key=?")) {
            preparedStatement.setObject(1, Utils.encode(identity), Types.CHAR);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    try (InputStream binaryStream = resultSet.getBinaryStream(1);
                         ObjectInputStream ois = new ObjectInputStream(binaryStream)) {
                        @SuppressWarnings("unchecked") OBJ<K> v = (OBJ<K>) ois.readObject();
                        v.setKey(identity);
                        return v;
                    }
                } else {
                    return null;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    protected <K, V extends OBJ<K>> V toObject(Object external, K identity) {
        //noinspection unchecked
        V v = beans.clone((V) external);
        v.setKey(identity);
        return v;
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

    @Override
    public <K, V extends OBJ<K>> Cache<K, V> cache(Class<? extends OBJ> clazz) {
        return new H2Cache<>(connection, clazz);
    }
}
