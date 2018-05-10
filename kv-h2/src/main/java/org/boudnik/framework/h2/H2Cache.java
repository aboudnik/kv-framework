package org.boudnik.framework.h2;

import org.boudnik.framework.OBJ;
import org.boudnik.framework.h2.H2Context.Type;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.Map;
import java.util.Set;

class H2Cache<K, V> extends H2AbstractCache<K, V> {

    private final H2Context context;
    private final Class<? extends OBJ> currentTable;

    H2Cache(H2Context context, Class<? extends OBJ> clazz) {
        this.context = context;
        currentTable = clazz;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        try {
            PreparedStatement merge = getPreparedStatement().get(Type.MERGE);
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
        PreparedStatement delete = getPreparedStatement().get(Type.DELETE);
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
        PreparedStatement delete = getPreparedStatement().get(Type.DELETE);
        try {
            String keyStr = Utils.encode(key);
            delete.setObject(1, keyStr, Types.CHAR);
            i = delete.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return i != 0;
    }


    private Map<Type, PreparedStatement> getPreparedStatement() {
        return context.getStatements().get(currentTable);
    }
}
