package it.r.ports.rest;

import com.google.common.collect.ImmutableSet;
import it.r.ports.api.CommunicationException;
import it.r.ports.api.Message;
import it.r.ports.api.Port;
import it.r.ports.rest.api.Header;
import it.r.ports.rest.api.Http;
import it.r.ports.rest.api.HttpMethod;
import lombok.Value;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static it.r.ports.rest.api.HttpMethod.*;
import static it.r.ports.rest.api.HttpMethod.GET;

public class JerseyAdapter implements Port {

    private final WebTarget client;

    public JerseyAdapter(WebTarget client) {
        this.client = client;
    }

    @Override
    public <T> T send(Message<T> message) throws CommunicationException {
        final Http http = message.getClass().getAnnotation(Http.class);

        final HttpRequest request = from(message);

        final Function<WebTarget, Invocation> configurer;
        switch (http.method()){
            case GET:
                configurer = get(request);
                break;
            case POST:
                configurer = post(request);
                break;
            default:
                throw new IllegalStateException("Whaat? " + http.method());
        }

        final Invocation invocation = configurer.apply(client);

        try {
            return invocation.invoke(message.responseType());
        }
        catch (ProcessingException | WebApplicationException e) {
            throw new CommunicationException("Error processing message: " + message, e);
        }
    }

    private Function<WebTarget, Invocation> post(HttpRequest message) {
        return client -> client
            .path(message.getHttp().path())
            .resolveTemplates(message.getPathParams())
            .request(MediaType.APPLICATION_JSON_TYPE)
            .headers(new MultivaluedHashMap<>(message.getHeaders()))
            .buildPost(
//                message.getHttp().multipart()
//                ? Entity.entity()
                Entity.json(message.getBody())
            );
    }

    private Function<WebTarget, Invocation> get(HttpRequest message) {
        return client -> message.getBody()
            .entrySet()
            .stream()
            .reduce(
                client,
                (t, e) -> t.queryParam(e.getKey(), e.getValue()),
                (t1, t2) -> t1
            )
            .path(message.getHttp().path())
            .resolveTemplates(message.getPathParams())
            .request(MediaType.APPLICATION_JSON_TYPE)
            .headers(new MultivaluedHashMap<>(message.getHeaders()))
            .buildGet();
    }


    private static final Map<Class, List<BiConsumer<Object, HttpRequest>>> CACHE = new HashMap<>();
    private static HttpRequest from(Object bean) {
        final HttpRequest result = new HttpRequest(bean.getClass().getAnnotation(Http.class));

        final List<BiConsumer<Object, HttpRequest>> copier = CACHE.computeIfAbsent(bean.getClass(), k -> {
            final Set<String> pathParams = UriSupport.tokens(result.getHttp().path());
            try {
                return Stream.of(Introspector.getBeanInfo(k).getPropertyDescriptors())
                    .filter(d -> !Objects.equals(d.getName(), "class"))
                    .map(d -> extractKey(d, pathParams))
                    .collect(Collectors.toList());

            } catch (IntrospectionException e) {
                throw new RuntimeException("Error processing: " + k, e);
            }
        });

        copier.stream()
            .forEach(c -> c.accept(bean, result));

        return result;
    }

    private static Object read(PropertyDescriptor descriptor, Object bean) {
        try {
            return descriptor.getReadMethod().invoke(bean);
        } catch (Exception e) {
            throw new RuntimeException("nono " + descriptor.getName() + " - " + bean, e);
        }
    }

    private static BiConsumer<Object, HttpRequest> extractKey(PropertyDescriptor descriptor, Set<String> queryParams) {
        final Set<Annotation> annotations;
        try {
            annotations = ImmutableSet.copyOf(descriptor.getReadMethod()
                .getDeclaringClass()
                .getDeclaredField(descriptor.getName())
                .getDeclaredAnnotations());

        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("What? " + descriptor.getName(), e);
        }

        if (annotations.contains(Header.class)) {
            return (msg, req) -> req.getHeaders().put(descriptor.getName(), read(descriptor, msg));
        }
        else if (queryParams.contains(descriptor.getName())) {
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
