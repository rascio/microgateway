package it.r.ports.utils.beans;

import com.google.common.collect.ImmutableList;
import it.r.ports.api.Request;
import lombok.AllArgsConstructor;
import org.apache.commons.collections.functors.ConstantFactory;

import java.beans.ConstructorProperties;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;

public class Introspection {

    /*
     *
     * Property Descriptors
     *
     */
    private static final Map<Class<?>, Map<String, PropertyDescriptor>> PROPS_DESCR_CACHE = new ConcurrentHashMap<>();

    public static Optional<PropertyDescriptor> read(Class<?> type, String property) {
        return Optional.ofNullable(PROPS_DESCR_CACHE.computeIfAbsent(type, Introspection::retrieveBeanInfo)
            .get(property));
    }
    public static Optional<Map<String, PropertyDescriptor>> read(Class<?> type) {
        return Optional.ofNullable(PROPS_DESCR_CACHE.computeIfAbsent(type, Introspection::retrieveBeanInfo));
    }

    private static Map<String, PropertyDescriptor> retrieveBeanInfo(Class<?> type) {
        try {
            return Stream.of(Introspector.getBeanInfo(type).getPropertyDescriptors())
                .collect(Collectors.toMap(PropertyDescriptor::getName, Function.identity()));
        } catch (IntrospectionException e) {
            throw new RuntimeException("What? " + type.getName(), e);
        }
    }


    /*
     *
     * Bean creation
     */
    public interface Factory<T> {
        T create(Map<String, Object> values);
        Class<?> typeOfParam(String param);
    };
    private static final Map<Integer, Optional<Factory<?>>> CONSTRUCTORS_CACHE = new ConcurrentHashMap<>();

    public static <T> Optional<Factory<T>> getFactory(Class<T> type, Set<String> strings) {
        return CONSTRUCTORS_CACHE.computeIfAbsent(Objects.hash(type, strings), k -> _findConstructor(type, strings)
            .map(c -> Optional.<Factory<?>>of(new ConstructorFactory<>(c)))
            .orElseGet(() -> {
                final Constructor<T> c;
                try {
                    c = type.getConstructor();
                } catch (NoSuchMethodException e) {
                    return Optional.empty();
                }
                return read(type)
                    .map(d -> new PropertyDescriptorFactory<>(c, d));
            })
        )
        .map(c -> (Factory<T>) c);
    }



    private static <T> Optional<Constructor<T>> _findConstructor(Class<T> type, Set<String> strings) {
        return Stream.of(type.getConstructors())
            .filter(c -> c.getAnnotation(ConstructorProperties.class) != null
                && !strings.containsAll(ImmutableList.of(c.getAnnotation(ConstructorProperties.class).value())))
            .min(comparing(c -> strings.size() - c.getAnnotation(ConstructorProperties.class).value().length))
            .map(c -> (Constructor<T>) c);
    }

    private static class ConstructorFactory<T> implements Factory<T> {
        private final Constructor<T> c;
        private final List<String> keys;
        private final List<Class> argsTypes;

        private ConstructorFactory(Constructor<T> c) {
            this.c = c;

            keys = Arrays.asList(c.getAnnotation(ConstructorProperties.class).value());
            argsTypes = Arrays.asList(c.getParameterTypes());
        }

        @Override
        public T create(Map<String, Object> args) {
            return newInstance(c, keys.stream()
                .map(args::get)
                .toArray()
            );
        }

        @Override
        public Class<?> typeOfParam(String param) {
            return argsTypes.get(keys.indexOf(param));
        }
    }

    @AllArgsConstructor
    private static class PropertyDescriptorFactory<T> implements Factory<T> {

        private final Constructor<T> constructor;
        private final Map<String, PropertyDescriptor> descriptors;

        @Override
        public T create(Map<String, Object> values) {
            final T instance = newInstance(constructor, new Object[0]);
            descriptors.forEach((f, d) -> invoke(d.getWriteMethod(), instance, values.get(f)));
            return null;
        }

        @Override
        public Class<?> typeOfParam(String param) {
            return descriptors.get(param).getPropertyType();
        }
    }

    private static <T> T newInstance(Constructor<T> c, Object...args) {
        try {
            return c.newInstance(args);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Not matching constructor:\n\t" + Arrays.toString(c.getParameterTypes()) + "\n\t" + Arrays.toString(args), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(c.toGenericString() + "is not accessible", e);
        } catch (InstantiationException | InvocationTargetException e) {
            throw new RuntimeException("What?", e);
        }
    }

    private static void invoke(Method m, Object o, Object...args) {
        try {
            m.invoke(o, args);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Not matching constructor:\n\t" + Arrays.toString(m.getParameterTypes()) + "\n\t" + Arrays.toString(args), e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("What?", e);
        }
    }


    /*
     *
     * Request utilities
     *
     */
    private static final Map<Class<?>, Class<?>> RESPONSES_CACHE = new ConcurrentHashMap<>();
    @SuppressWarnings("unchecked")
    public static final <M extends Request<?, ?, ?, R>, R> Class<R> responseType(Class<M> type) {
        return (Class<R>) RESPONSES_CACHE.computeIfAbsent(type, k -> Stream.of(k.getGenericInterfaces())
            .filter(ParameterizedType.class::isInstance)
            .map(ParameterizedType.class::cast)
            .filter(t -> t.getRawType() instanceof Class)
            .filter(t -> Request.class.isAssignableFrom((Class<?>) t.getRawType()))
            .map(t -> t.getActualTypeArguments()[t.getActualTypeArguments().length - 1])
            .map(t -> {
                if (t instanceof Class) {
                    return (Class<?>)t;
                } else if (t instanceof ParameterizedType) {
                    return (Class<?>)((ParameterizedType) t).getRawType();
                } else {
                    throw new RuntimeException("Not managed type: " + t.getTypeName());
                }
            })
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Does " + type.getName() + " implements Request?")));
    }

}
