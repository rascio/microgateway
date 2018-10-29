package it.r.ports.utils;

import com.google.common.collect.ImmutableList;
import it.r.ports.api.Request;

import java.beans.ConstructorProperties;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;

public class Introspection {

    private static final Map<Class<?>, Map<String, PropertyDescriptor>> PROPS_DESCR_CACHE = new ConcurrentHashMap<>();

    public static Optional<PropertyDescriptor> read(Class<?> type, String property) {
        return Optional.ofNullable(PROPS_DESCR_CACHE.computeIfAbsent(type, retrieveBeanInfo(type))
            .get(property));
    }

    private static Function<Class<?>, Map<String, PropertyDescriptor>> retrieveBeanInfo(Class<?> type) {
        return k -> {
            try {
                return Stream.of(Introspector.getBeanInfo(type).getPropertyDescriptors())
                    .collect(Collectors.toMap(PropertyDescriptor::getName, Function.identity()));
            } catch (IntrospectionException e) {
                throw new RuntimeException("What? " + type.getName(), e);
            }
        };
    }

    private static final Map<Integer, Optional<Constructor<?>>> CONSTRUCTORS_CACHE = new ConcurrentHashMap<>();

    public static <T> Optional<Constructor<T>> findConstructor(Class<T> type, Set<String> strings) {
        return CONSTRUCTORS_CACHE.computeIfAbsent(Objects.hash(type, strings), _findConstructor(type, strings))
            .map(c -> (Constructor<T>) c);

    }
    private static Function<Integer, Optional<Constructor<?>>> _findConstructor(Class<?> type, Set<String> strings) {
        return k -> Stream.of(type.getConstructors())
            .filter(c -> c.getAnnotation(ConstructorProperties.class) != null
                && !strings.containsAll(ImmutableList.of(c.getAnnotation(ConstructorProperties.class).value())))
            .min(comparing(c -> strings.size() - c.getAnnotation(ConstructorProperties.class).value().length));
    }

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
