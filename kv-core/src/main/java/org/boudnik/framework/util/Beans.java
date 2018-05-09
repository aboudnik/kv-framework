package org.boudnik.framework.util;

import org.boudnik.framework.TenacityException;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Utility class
 *
 * @author Alexandre_Boudnik
 * @since 04/10/18 15:46
 */
public class Beans {

    private final Map<Class, BeanInfo> meta = new HashMap<>();

    //    public <T extends OBJ> boolean isEquals(T b1, T b2) throws IllegalAccessException, InvocationTargetException, IntrospectionException {
    public <T> boolean equals(T b1, T b2) {
        try {
            for (PropertyDescriptor descriptor : getBeanInfo(b1.getClass()).getPropertyDescriptors()) {
                Method get = descriptor.getReadMethod();
                if (!Objects.equals(get.invoke(b1), get.invoke(b2)))
                    return false;
            }
            return true;
        } catch (IntrospectionException | IllegalAccessException | InvocationTargetException e) {
            throw new TenacityException(e);
        }
    }

    //    public <T> T set(T src, T dst) throws IllegalAccessException, InvocationTargetException, IntrospectionException {
    public <T> T set(T src, T dst) {
        try {
            for (PropertyDescriptor descriptor : getBeanInfo(src.getClass()).getPropertyDescriptors()) {
                Method get = descriptor.getReadMethod();
                Method set = descriptor.getWriteMethod();
                if (set != null)
                    set.invoke(dst, get.invoke(src));
            }
            return dst;
        } catch (IntrospectionException | IllegalAccessException | InvocationTargetException e) {
            throw new TenacityException(e);
        }
    }

    @SuppressWarnings("unchecked")
//    public <T extends OBJ> T clone(T src) throws InstantiationException, IllegalAccessException, IntrospectionException, InvocationTargetException {
    public <T> T clone(T src) {
        try {
            return set(src, ((Class<T>) src.getClass()).newInstance());
        } catch (IllegalAccessException | InstantiationException e) {
            throw new TenacityException(e);
        }
    }

    private BeanInfo getBeanInfo(Class clazz) throws IntrospectionException {
        BeanInfo info = meta.get(clazz);
        if (info == null)
            meta.put(clazz, info = Introspector.getBeanInfo(clazz));
        return info;
    }
}
