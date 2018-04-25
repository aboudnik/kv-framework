package org.boudnik.framework.util;

import org.boudnik.framework.OBJ;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

import static java.beans.Introspector.getBeanInfo;

/**
 * Utility class
 *
 * @author Alexandre_Boudnik
 * @since 04/10/18 15:46
 */
public class Beans {
    public static <T> boolean isEquals(BeanInfo beanInfo, T b1, T b2) throws IllegalAccessException, InvocationTargetException {
        for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {
            Method get = descriptor.getReadMethod();
            if (!Objects.equals(get.invoke(b1), get.invoke(b2)))
                return false;
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

    public static void set(Map<Class, BeanInfo> meta, Object src, Object dst) throws IllegalAccessException, InvocationTargetException, IntrospectionException {
        for (PropertyDescriptor descriptor : getBeanInfo(meta, src.getClass()).getPropertyDescriptors()) {
            Method get = descriptor.getReadMethod();
            Method set = descriptor.getWriteMethod();
            if (set != null)
                set.invoke(dst, get.invoke(src));
        }
    }

    private static BeanInfo getBeanInfo(Map<Class, BeanInfo> meta, Class clazz) throws IntrospectionException {
        BeanInfo info = meta.get(clazz);
        if (info == null)
            meta.put(clazz, info = Introspector.getBeanInfo(clazz));
        return info;
    }
}
