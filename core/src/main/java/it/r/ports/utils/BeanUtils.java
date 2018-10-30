package it.r.ports.utils;

import com.google.common.collect.ImmutableList;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.PropertyUtilsBean;

import java.beans.ConstructorProperties;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static it.r.ports.utils.Introspection.findConstructor;
import static java.util.Comparator.comparing;

public class BeanUtils {

    public static <T> T copyProperties(Object source, T dest) {
        try {
            BeanUtilsBean.getInstance().copyProperties(dest, source);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Error while copying properties from %s to %s", source, dest), e);
        }
        return dest;
    }

    @SuppressWarnings("unchecked")
    public static <T> void updatePropertiesOfType(Object source, Class<T> type, Function<T, T> fn) {
        BeanUtilsBean beanUtils = BeanUtilsBean.getInstance();
        PropertyUtilsBean propertyUtils = beanUtils.getPropertyUtils();
        PropertyDescriptor[] origDescriptors = propertyUtils.getPropertyDescriptors(source);
        for (int i = 0; i < origDescriptors.length; i++) {
            String name = origDescriptors[i].getName();
            Class<?> propertyType = origDescriptors[i].getPropertyType();
            if ("class".equals(name) || !type.isAssignableFrom(propertyType)) {
                continue;
            }
            if (propertyUtils.isReadable(source, name) &&
                propertyUtils.isWriteable(source, name)) {
                try {
                    T value = (T) propertyUtils.getSimpleProperty(source, name);
                    value = fn.apply(value);
                    setProperty(source, name, value);
                } catch (Exception e) {
                    throw new RuntimeException("Error while updating property " + name + " of bean " + source, e);
                }
            }
        }
    }

    public static <T> T populate(T target, Map<String, ?> props) {
        try {
            org.apache.commons.beanutils.BeanUtils.populate(target, props);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Error while copying properties from %s to %s", target, props), e);
        }
        return target;
    }

    public static void setProperty(Object bean, String property, Object value) {
        try {
            BeanUtilsBean.getInstance().setProperty(bean, property, value);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Error while setting property %s to %s with value %s", property, bean, value), e);
        }
    }

    public static <T> T newInstance(Class<T> type) {
        try {
            Constructor<T> c = type.getDeclaredConstructor();
            c.setAccessible(true);
            T i = c.newInstance();
            c.setAccessible(false); //TODO: understand if its really needed and remove it if it isnt
            return i;
        } catch (Exception e) {
            throw new RuntimeException("Error while creating an instance of " + type, e);
        }
    }

    public static <T> T newInstance(Class<T> type, Map<String, Object> values) {
        return findConstructor(type, values.keySet())
            .map(c -> newInstance(c, values))
            .orElseGet(() -> {
                final T instance = newInstance(type);
                populate(instance, values);
                return instance;
            });
    }

    private static <U> U newInstance(Constructor<U> c, Map<String, Object> objects) {
        final Class<?>[] types = c.getParameterTypes();
        final String[] names = c.getAnnotation(ConstructorProperties.class).value();
        final Object[] convertedValues = new Object[types.length];

        for (int i = 0; i < types.length; i++) {
            convertedValues[i] = ConvertUtils.convert(objects.get(names[i]), types[i]);
        }

        try {
            return c.newInstance(convertedValues);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Not matching constructor:\n\t" + Arrays.toString(types) + "\n\t" + Arrays.toString(convertedValues) , e);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException("What?", e);
        }
    }
}
