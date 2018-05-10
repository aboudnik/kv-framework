package org.boudnik.framework.h2;

import org.boudnik.framework.Context;
import org.boudnik.framework.OBJ;
import org.boudnik.framework.h2.Utils.QueryType;

import javax.cache.Cache;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class H2Context extends Context {

    private final Connection connection;
    private final Map<Class<? extends OBJ>, Map<QueryType, PreparedStatement>> statements = new HashMap<>();

    public H2Context(Connection connection) {
        this.connection = connection;
    }

    public H2Context withTable(Class... classes) {
        for (Class aClass : classes) {
            try {
                PreparedStatement select = connection.prepareStatement("SELECT value FROM " + aClass.getSimpleName() + " WHERE key = ?");
                PreparedStatement delete = connection.prepareStatement("DELETE FROM " + aClass.getSimpleName() + " WHERE key = ?");
                PreparedStatement merge = connection.prepareStatement("MERGE INTO " + aClass.getSimpleName() + " KEY(key) VALUES(?, ?)");
                Map<QueryType, PreparedStatement> queries = new HashMap<>(3);
                queries.put(QueryType.SELECT, select);
                queries.put(QueryType.DELETE, delete);
                queries.put(QueryType.MERGE, merge);
                //noinspection unchecked
                statements.put(aClass, queries);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return this;
    }

    @Override
    protected <K> Object getNative(Class<? extends OBJ> clazz, K identity) throws Exception {
        PreparedStatement select = statements.get(clazz).get(QueryType.SELECT);
        select.setObject(1, Utils.encode(identity), Types.CHAR);
        try (ResultSet resultSet = select.executeQuery()) {
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
    public void close() {
        super.close();
        statements.forEach((table, queries) -> closeStatements(queries));
        statements.clear();
        closeConnection(connection);
    }

    @Override
    public <K, V extends OBJ<K>> Cache<K, V> cache(Class<? extends OBJ> clazz) {
        return new H2Cache<>(this, clazz);
    }

    Map<Class<? extends OBJ>, Map<QueryType, PreparedStatement>> getStatements() {
        return statements;
    }

    private void closeStatements(Map<?, PreparedStatement> queries) {
        queries.forEach((type, ps) -> {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void closeConnection(Connection c) {
        if (c != null) {
            try {
                c.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
