package it.r.ports.rest;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.sun.xml.internal.bind.v2.util.QNameMap;
import it.r.ports.api.CommunicationException;
import it.r.ports.api.Message;
import it.r.ports.api.Gateway;
import it.r.ports.api.Request;
import it.r.ports.rest.api.Header;
import it.r.ports.rest.api.Http;
import it.r.ports.rest.api.RestApiRegistry;
import lombok.Value;
import org.apache.commons.beanutils.BeanMap;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JerseyAdapter implements Gateway {

    private final WebTarget client;
    private final RestApiRegistry restApiRegistry;

    public JerseyAdapter(WebTarget client, RestApiRegistry restApiRegistry) {
        this.client = client;
        this.restApiRegistry = restApiRegistry;
    }


    @Override
    public <I, P, B, T> T send(Request<I, P, B, T> message) {
        final Http http = restApiRegistry.find(message.getClass())
            .orElseThrow(() -> new IllegalStateException("Not managed " + message.getClass().getName()));

        final Function<WebTarget, Invocation> configurer;
        switch (http.getMethod()){
            case GET:
                configurer = get(message, http);
                break;
            case POST:
                configurer = post(message, http);
                break;
            default:
                throw new IllegalStateException("Whaat? " + http.getMethod());
        }

        final Invocation invocation = configurer.apply(client);

        try {
            return invocation.invoke(message.responseType());
        }
        catch (ProcessingException | WebApplicationException e) {
            throw new CommunicationException("Error processing message: " + message, e);
        }
    }

    private Function<WebTarget, Invocation> post(Request<?, ?, ?, ?> req, Http http) {
        return client -> client
            .path(http.getPath())
            .resolveTemplates(toMap(req.getId()))
            .request(MediaType.APPLICATION_JSON_TYPE)
//            .headers(new MultivaluedHashMap<>(message.getHeaders()))
            .buildPost(
//                message.getHttp().multipart()
//                ? Entity.entity()
                Entity.json(req.getBody())
            );
    }

    private Map<String, Object> toMap(Object id) {
        if (id instanceof Map) {
            return (Map<String, Object>) id;
        }
        else {
            return ImmutableMap.of("id", id);
        }
    }

    private Function<WebTarget, Invocation> get(Request<?, ?, ?, ?> req, Http http) {
        return client -> beanMap(req.getParameters())
            .entrySet()
            .stream()
            .reduce(
                client,
                (t, e) -> t.queryParam(e.getKey().toString(), e.getValue()),
                (t1, t2) -> t1
            )
            .path(http.getPath())
            .resolveTemplates(toMap(req.getId()))
            .request(MediaType.APPLICATION_JSON_TYPE)
//            .headers(new MultivaluedHashMap<>(message.getHeaders()))
            .buildGet();
    }

    private Map<String, Object> beanMap(Object parameters) {
        final BeanMap map = new BeanMap(parameters);
        final Map<String, Object> res = new HashMap(map);
        res.remove("class");

        return res;
    }


    private static final Map<Class, List<BiConsumer<Object, HttpRequest>>> CACHE = new HashMap<>();
    private static HttpRequest from(Object bean, Http http) {
        final HttpRequest result = new HttpRequest(http);

        final List<BiConsumer<Object, HttpRequest>> copier = CACHE.computeIfAbsent(bean.getClass(), k -> {
            final Set<String> pathParams = UriSupport.tokens(result.getHttp().getPath());
            try {
                return Stream.of(Introspector.getBeanInfo(k).getPropertyDescriptors())
                    .filter(d -> !Objects.equals(d.getName(), "class"))
                    .map(d -> extractKey(d, pathParams))
                    .collect(Collectors.toList());

            } catch (IntrospectionException e) {
                throw new RuntimeException("Error processing: " + k, e);
            }
        });

        copier.forEach(c -> c.accept(bean, result));

        return result;
    }

    private static Object read(PropertyDescriptor descriptor, Object bean) {
        try {
            return descriptor.getReadMethod().invoke(bean);
        } catch (Exception e) {
            throw new RuntimeException("read what? " + descriptor.getName() + " - " + bean, e);
        }
    }

    private static BiConsumer<Object, HttpRequest> extractKey(PropertyDescriptor descriptor, Set<String> pathParams) {
        final Set<Annotation> annotations;
        try {
            annotations = ImmutableSet.copyOf(descriptor.getReadMethod()
                .getDeclaringClass()
                .getDeclaredField(descriptor.getName())
                .getDeclaredAnnotations());

        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("extractKey from what? " + descriptor.getName(), e);
        }

        if (annotations.contains(Header.class)) {
            return (msg, req) -> req.getHeaders().put(descriptor.getName(), read(descriptor, msg));
        }
        else if (pathParams.contains(descriptor.getName())) {
            return (msg, req) -> req.getPathParams().put(descriptor.getName(), read(descriptor, msg));
        }
        else {
            return (msg, req) -> req.getBody().put(descriptor.getName(), read(descriptor, msg));
        }
    }

    @Value
    private static final class HttpRequest {
        Http http;
        Map<String, Object> pathParams = new HashMap<>();
        Map<String, Object> headers = new HashMap<>();
        Map<String, Object> body = new HashMap<>();
    }
}
