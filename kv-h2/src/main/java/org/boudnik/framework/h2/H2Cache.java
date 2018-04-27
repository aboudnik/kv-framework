package org.boudnik.framework.h2;

import org.boudnik.framework.OBJ;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;
import java.util.Set;

class H2Cache<K, V> extends H2AbstractCache<K, V> {

    private final PreparedStatement removePs;
    private final PreparedStatement putPs;

    H2Cache(Connection connection, Class<? extends OBJ> clazz) {
        try {
            removePs = connection.prepareStatement("DELETE FROM " + clazz.getSimpleName() + " WHERE key = ?");
            putPs = connection.prepareStatement("MERGE INTO " + clazz.getSimpleName() + " KEY(key) VALUES(?, ?)");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        try {
            for (Map.Entry<? extends K, ? extends V> entries : map.entrySet()) {
                putPs.setObject(1, entries.getKey(), Types.JAVA_OBJECT);
                putPs.setObject(2, entries.getValue(), Types.JAVA_OBJECT);
                putPs.addBatch();
            }
            putPs.executeBatch();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeAll(Set<? extends K> keys) {
        try {
            for (K key : keys) {
                removePs.setObject(1, key, Types.JAVA_OBJECT);
                removePs.addBatch();
            }
            removePs.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean remove(K key) {
        int i;
        try {
            removePs.setObject(1, key, Types.JAVA_OBJECT);
            i = removePs.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return i != 0;
    }
}
