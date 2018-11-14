package it.r.ports.hypermedia.api;

import it.r.ports.api.DefaultGateway.GatewayWrapper;
import it.r.ports.api.Envelope;
import it.r.ports.api.Gateway;
import it.r.ports.api.Request;
import org.apache.commons.beanutils.MethodUtils;
import org.slf4j.Logger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;


public class AuthorizationGateway implements Gateway {

    private static final Logger LOGGER = getLogger(AuthorizationGateway.class);

    private static final RequestFilter AUTHORIZED = (m, g) -> true;
    private final Map<Class<? extends Request<?, ?, ?, ?>>, RequestFilter> filters;
    private final Gateway gateway;

    private AuthorizationGateway(Gateway gateway, Map<Class<? extends Request<?, ?, ?, ?>>, RequestFilter> filters) {
        this.filters = filters;
        this.gateway = gateway;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public <R> R send(Envelope<? extends Request<?, ?, ?, R>> message) {
        if (checkAuth(message)) {
            final R result = gateway.send(message);
            if (result instanceof Mono) {
                return (R) ((Mono) result).map(v -> inspect(v, message));
            }
            else if (result instanceof Flux) {
                return (R) ((Flux) result).map(v -> inspect(v, message));
            }
            return (R) inspect(result, message);
        }
        else {
            throw new RuntimeException("Not authorized!");
        }
    }


    private Object inspect(Object value, Envelope<?> message) {
        /*
         *  TODO: cached inspectors
         *  Inspect the class and compose an inspector just for the right keys
         *  for DTOs, List, etc.. and cache them
         *
         *  Registration of new filters for new types ("primitive" like Iterable and Map)
         */
        LOGGER.trace("Inspect: [{}] {}", value.getClass().getName(), value);
        if (value == null
            || value.getClass().isPrimitive()
            || value instanceof String
            || value instanceof Class) {
            LOGGER.trace("is primitive");
            return value;
        }
        else if (value.getClass().isArray()) {
            LOGGER.trace("is array");
            filter(Arrays.asList((Object[]) value), message);
        }
        else if (value instanceof Iterable) {
            LOGGER.trace("is iterable");
            filter((Iterable<?>) value, message);
        }
        else if (value instanceof Map) {
            LOGGER.trace("is map");
            filter((Map<?, ?>) value, message);
        }
        else {
            LOGGER.trace("is object");
            filter(value, message);
        }

        if (value instanceof Resource) {
            LOGGER.trace("is resource");
            filter((Resource) value, message);
        }

        return value;
    }

    private void filter(Resource value, Envelope<?> message) {
        value.filter(req -> checkAuth(message.withRequest(req)));
    }

    private void filter(Object value, Envelope<?> message) {
        try {
            for (PropertyDescriptor pe : Introspector.getBeanInfo(value.getClass()).getPropertyDescriptors()) {
                inspect(pe.getReadMethod().invoke(value), message);
            }
        } catch (IntrospectionException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("What? " + value.getClass(), e);
        }
    }

    private void filter(Map<?, ?> value, Envelope<?> message) {
        value.forEach((k, v) -> {
            inspect(k, message);
            inspect(v, message);
        });
    }

    private void filter(Iterable<?> value, Envelope<?> message) {
        value.forEach(v -> inspect(v, message));
    }


    private <R, T extends Request<?, ?, ?, R>> boolean checkAuth(Envelope<T> message) {
        final RequestFilter filter = handlerFor(message);

        return filter.authorized(message, gateway);
    }

    private <R, T extends Request<?, ?, ?, R>> RequestFilter handlerFor(Envelope<T> message) {
        return filters.getOrDefault(message.getRequest().getClass(), AUTHORIZED);
    }

    public static class Builder {
        private final Map<Class<? extends Request<?, ?, ?, ?>>, RequestFilter> filters = new HashMap<>();

        public <T extends Request<?, ?, ?, ?>> Builder authorize(Class<T> type, RequestFilter<T> filter) {
            filters.put(type, filter);
            return this;
        }

        public GatewayWrapper create() {
            return gateway -> new AuthorizationGateway(gateway, filters);
        }
    }
}
