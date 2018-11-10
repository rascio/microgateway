package it.r.ports.utils.beans;

import com.google.common.base.Joiner;
import it.r.ports.utils.beans.Introspection.Factory;
import it.r.ports.utils.beans.MappingException.FailedMapping;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtils;

import java.beans.ConstructorProperties;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.Map.Entry;

import static it.r.ports.utils.beans.Introspection.getFactory;
import static it.r.ports.utils.beans.Introspection.responseType;
import static java.util.Comparator.comparing;

public class BeanUtils {

    public static <T> T newInstance(Class<T> type) {
        Constructor<T> c;
        try {
            c = type.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(type.getName() + " miss an empty constructor", e);
        }
        try {
            T i = c.newInstance();
            return i;
        } catch (InstantiationException | InvocationTargetException e) {
            throw new RuntimeException("Error while creating an instance of " + type, e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(c.toGenericString() + " should be public, and accessible", e);
        }
    }

    public static <T> T newInstance(Class<T> type, Map<String, Object> values) throws MappingException {
        final Factory<T> factory = Introspection.getFactory(type, values.keySet())
            .orElseThrow(() -> new RuntimeException(String.format("Don't know how to instantiate %s[%s], create a constructor with parameters or add a @Value", type.getName(), Joiner.on(", ").join(values.keySet()))));

        final Map<String, Object> args = new HashMap<>();
        final List<FailedMapping> errors = new LinkedList<>();
        values.forEach((field, value) -> {
            try {
                args.put(field, ConvertUtils.convert(value, factory.typeOfParam(field)));
            } catch (ConversionException ex) {
                errors.add(new FailedMapping(field, value, ex));
            }
        });

        MappingException.checkNoErrors(errors, type);

        return factory.create(values);
    }

}
