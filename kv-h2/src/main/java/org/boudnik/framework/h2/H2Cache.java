package org.boudnik.framework.h2;

import org.boudnik.framework.OBJ;
import org.boudnik.framework.h2.Utils.QueryType;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;
import java.util.Set;

class H2Cache<K, V extends OBJ<K>> extends H2AbstractCache<K, V> {

    private final H2Context context;
    private final Class<? extends OBJ> clazz;

    H2Cache(H2Context context, Class<? extends OBJ> clazz) {
        this.context = context;
        this.clazz = clazz;
    }

    @Override
    public V get(K key) {
        try (ResultSet resultSet = getPreparedSelect(key).executeQuery()) {
            if (resultSet.next()) {
                try (InputStream binaryStream = resultSet.getBinaryStream(1);
                     ObjectInputStream ois = new ObjectInputStream(binaryStream)) {
                    @SuppressWarnings("unchecked") V v = (V) ois.readObject();
                    v.setKey(key);
                    return v;
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private PreparedStatement getPreparedSelect(K key) throws SQLException {
        PreparedStatement select = getPreparedStatement().get(QueryType.SELECT);
        select.setString(1, Utils.encode(key));
        return select;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        try {
            PreparedStatement merge = getPreparedStatement().get(QueryType.MERGE);
            for (Map.Entry<? extends K, ? extends V> entries : map.entrySet()) {
                K key = entries.getKey();
                String keyStr = Utils.encode(key);
                merge.setObject(1, keyStr, Types.CHAR);
                merge.setObject(2, entries.getValue(), Types.JAVA_OBJECT);
                merge.addBatch();
            }
            merge.executeBatch();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeAll(Set<? extends K> keys) {
        PreparedStatement delete = getPreparedStatement().get(QueryType.DELETE);
        try {
            for (K key : keys) {
                String keyStr = Utils.encode(key);
                delete.setObject(1, keyStr, Types.CHAR);
                delete.addBatch();
            }
            delete.executeBatch();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean remove(K key) {
        int i;
        PreparedStatement delete = getPreparedStatement().get(QueryType.DELETE);
        try {
            String keyStr = Utils.encode(key);
            delete.setObject(1, keyStr, Types.CHAR);
            i = delete.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return i != 0;
    }

    private Map<QueryType, PreparedStatement> getPreparedStatement() {
        return context.getStatements().get(clazz);
    }
}
