package org.boudnik.framework;

import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Utility class
 *
 * @author Alexandre_Boudnik
 * @since 04/10/18 15:46
 */
public class Introspector {
    public static <T> boolean isEquals(BeanInfo beanInfo, T b1, T b2) throws IllegalAccessException, InvocationTargetException {
        for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {
            Method get = descriptor.getReadMethod();
            if (!Objects.equals(get.invoke(b1), get.invoke(b2)))
                return false;
        }
        return true;
    }

    public static <T> boolean isEquals(T b1, T b2) throws IllegalAccessException {
        
        for (Field field: b1.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if(!Objects.equals(field.get(b1), field.get(b2))){
                return false;
            }
        }
        return true;
    }

    public static <T> void set(BeanInfo beanInfo, T src, T dst) throws IllegalAccessException, InvocationTargetException {
        for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {
            Method get = descriptor.getReadMethod();
            Method set = descriptor.getWriteMethod();
            if (set != null)
                set.invoke(dst, get.invoke(src));
        }
    }
}
