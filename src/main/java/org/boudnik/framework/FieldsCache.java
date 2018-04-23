package org.boudnik.framework;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Sergey Nuyanzin
 * @since 4/23/2018
 */
public class FieldsCache {
    private final ConcurrentMap<Class, Collection<Field>> fieldsMap = new ConcurrentHashMap<>();
    private FieldsCache() {
    }

    private static final class FieldsCacheHolder {
        private static final FieldsCache FIELDS_CACHE_INSTANCE = new FieldsCache();
    }

    public static FieldsCache getInstance(){
        return FieldsCacheHolder.FIELDS_CACHE_INSTANCE;
    }

    public Collection<Field> getFields(Class clazz){
        /*
        fieldsMap.containsKey == true && fieldsMap.get(clazz) == null
        boundary case with an attempt to get info for not found class
         */
        if(fieldsMap.get(clazz) == null) {
            if(fieldsMap.containsKey(clazz)){
               return Collections.emptyList();
            }
            Field[] fieldCollection = clazz.getDeclaredFields();
            for(Field field: fieldCollection){
                field.setAccessible(true);
            }
            Collection<Field> collection = Collections.unmodifiableCollection(Arrays.asList(fieldCollection));
            fieldsMap.put(clazz, collection);
            return collection;
        }
        return fieldsMap.get(clazz);
    }
}
