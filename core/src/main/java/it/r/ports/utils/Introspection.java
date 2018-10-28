package it.r.ports.utils;

import lombok.SneakyThrows;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Introspection {

    private static final Map<Class<?>, Map<String, PropertyDescriptor>> CACHE = new ConcurrentHashMap<>();

    public static Optional<PropertyDescriptor> read(Class<?> type, String property) {
        return Optional.ofNullable(CACHE.computeIfAbsent(type, retrieveBeanInfo(type))
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
}
