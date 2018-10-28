package it.r.microgateway.server.url;

import com.google.common.collect.ImmutableMap;
import it.r.ports.api.Gateway;
import it.r.ports.api.Message;
import it.r.ports.api.Request;
import it.r.ports.rest.api.Http;
import it.r.ports.rest.api.HttpMethod;
import it.r.ports.rest.api.RestApiRegistry;
import it.r.ports.utils.BeanUtils;
import it.r.ports.utils.Introspection;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.*;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.web.reactive.function.BodyInserters.fromObject;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;

public class MessageRouterBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageRouterBuilder.class);

    public static RouterFunction<ServerResponse> create(RestApiRegistry registry, Gateway gateway, ConversionService conversionService) {
        final Builder builder = new Builder(conversionService);
        return registry.getApis()
            .entrySet()
            .stream()
            .map(e -> builder.handlerFor(e.getKey(), gateway, e.getValue()))
            .reduce(RouterFunction::and)
            .orElseThrow(() -> new IllegalArgumentException("Empty registry! " + registry));

    }

    @AllArgsConstructor
    private static class Builder {
        private final ConversionService conversionService;

        private RouterFunction<ServerResponse> handlerFor(Class<? extends Request> type, Gateway gateway, Http http) {

            LOGGER.info("Registering handler for {}", type.getName());

            final RouterFunction<ServerResponse> route;
            if (http.getMethod() == HttpMethod.GET) {
                route = RouterFunctions.route(GET(http.getPath()), createGETHandler(type, gateway));
            }
            else if (http.getMethod() == HttpMethod.POST && !http.isMultipart()) {
                route = RouterFunctions.route(POST(http.getPath()), createPOSTHandler(type, gateway));
            }
            else {
                throw new IllegalStateException("What? " + type.getName());
            }

            return route;

        }

        private HandlerFunction<ServerResponse> createPOSTHandler(Class<? extends Request> type, Gateway gateway) {
            return request -> request.bodyToMono(typeOfField(type, "body"))
                .doOnNext(msg -> LOGGER.info("Received: {}", msg))
                .map(body -> {
                    final Request r = BeanUtils.newInstance(type, ImmutableMap.of(
                        "body", body,
                        "id", convertId(request, typeOfField(type, "id"))
//                        "parameters", conversionService.convert(request.queryParams(), typeOfField(type, "parameters"))
                    ));
                    return r;
                })
                .map(msg -> gateway.send(msg))
                .flatMap(resp -> ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(fromObject(resp))
                )
                .switchIfEmpty(ServerResponse.notFound()
                    .build()
                );
        }

        private Object convertId(ServerRequest request, Class<?> type) {
            final Map<String, String> params = request.pathVariables();
            if (params.size() == 1) {
                return conversionService.convert(params.values().iterator().next(), type);
            }
            return conversionService.convert(params, type);
        }

        private static Class<?> typeOfField(Class<? extends Request> type, String body) {
            return Introspection.read(type, body)
                .map(PropertyDescriptor::getPropertyType)
                .orElseThrow(() -> new RuntimeException("Missing " + type.getName() + "." + body));
        }

        private HandlerFunction<ServerResponse> createGETHandler(Class<? extends Request> type, Gateway gateway) {
            return request -> {
                LOGGER.info("Handling request");
                final Request r = BeanUtils.newInstance(type, ImmutableMap.of(
                    "id", convertId(request, typeOfField(type, "id")),
                    "parameters", BeanUtils.newInstance(typeOfField(type, "parameters"), toSimpleMap(request.queryParams()))
                ));
                LOGGER.info("Received {}", r);

                final Object response = gateway.send(r);

                return ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(fromObject(response));
            };
        }

        private Map<String, Object> toSimpleMap(MultiValueMap<String, ?> parameters) {
            final Map<String, Object> m = new HashMap<>();
            parameters.forEach((k, l) -> {
                if (l.size() > 1) {
                    m.put(k, l);
                } else {
                    m.put(k, l.get(0));
                }
            });
            return m;
        }

    }
}
