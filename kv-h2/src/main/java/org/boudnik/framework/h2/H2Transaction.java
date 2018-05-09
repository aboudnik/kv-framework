package org.boudnik.framework.h2;

import org.boudnik.framework.Context;
import org.boudnik.framework.OBJ;
import org.boudnik.framework.util.Beans;

import javax.cache.Cache;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.sql.*;
import java.util.Map;

public class H2Transaction extends Context {

    private final Connection connection;

    public H2Transaction(Connection connection) {
        this.connection = connection;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <K, V extends OBJ> V get(Class<V> clazz, K identity) {
        final Map<Object, OBJ> map = getMap(clazz);
        V obj = (V) map.get(identity);
        if (obj == null) {
            try {
                PreparedStatement preparedStatement =
                        connection.prepareStatement("SELECT value FROM " + clazz.getSimpleName() + " WHERE key=?");
                String keyStr = Utils.encode(identity);
                preparedStatement.setObject(1, keyStr, Types.CHAR);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    try (InputStream binaryStream = resultSet.getBinaryStream(1);
                         ObjectInputStream ois = new ObjectInputStream(binaryStream)) {
                        V v = (V) ois.readObject();
                        v.setKey(identity);
                        map.put(identity, v);
                        mementos.put(identity, Beans.clone(meta, v));
                        return v;
                    }
                } else {
                    return null;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            return obj;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected OBJ<Object> getMementoValue(Map.Entry<Object, Object> memento) {
        return (OBJ<Object>) memento.getValue();
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

    @SuppressWarnings("unchecked")
    @Override
    protected <K, V, T extends Cache<K, V>> T cache(Class<? extends OBJ> clazz) {
        return (T) new H2Cache<K, V>(connection, clazz);
    }
}
