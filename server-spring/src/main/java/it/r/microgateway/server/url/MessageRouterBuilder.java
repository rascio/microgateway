package it.r.microgateway.server.url;

import com.google.common.collect.ImmutableMap;
import it.r.ports.api.Message;
import it.r.ports.rest.api.Http;
import it.r.ports.rest.api.HttpMethod;
import it.r.ports.utils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.function.Function;

import static org.springframework.web.reactive.function.BodyInserters.fromObject;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;

public class MessageRouterBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageRouterBuilder.class);
    private final RouterFunction<ServerResponse> router;

    public static MessageRouterBuilder create() {
        return new MessageRouterBuilder(null);
    }

    public MessageRouterBuilder(RouterFunction<ServerResponse> router) {
        this.router = router;
    }

    public <T extends Message<R>, R> MessageRouterBuilder handlerFor(Class<T> type, Function<T, R> handler) {
        final Http http = type.getAnnotation(Http.class);

        LOGGER.info("Registering handler for {}", type.getName());

        final RouterFunction<ServerResponse> route;
        if (http.method() == HttpMethod.GET) {
            route = RouterFunctions.route(GET(http.path()), createGETHandler(type, handler));
        }
        else if (http.method() == HttpMethod.POST && !http.multipart()) {
            route = RouterFunctions.route(POST(http.path()), createPOSTHandler(type, handler));
        }
        else {
            throw new IllegalStateException("What? " + type.getName());
        }

        if (router == null) {
            return new MessageRouterBuilder(route);
        } else {
            return new MessageRouterBuilder(router.and(route));
        }
    }

    private <T extends Message<R>, R> HandlerFunction<ServerResponse> createPOSTHandler(Class<T> type, Function<T, R> handler) {
        return request -> request.bodyToMono(type)
            .map(instance -> BeanUtils.populate(instance, request.pathVariables()))
            .map(handler)
            .flatMap(resp -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(fromObject(resp))
            )
            .switchIfEmpty(ServerResponse.notFound()
                .build()
            );
    }

    private <T extends Message<R>, R> HandlerFunction<ServerResponse> createGETHandler(Class<T> type, Function<T, R> handler) {
        return request -> {
            LOGGER.info("Handling request");
            final T message = BeanUtils.newInstance(type, ImmutableMap.<String, Object>builder()
                .putAll(request.pathVariables())
                .putAll(request.queryParams())
                //TODO manage headers and query params properly
                .build()
            );

            final R response = handler.apply(message);

            return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(fromObject(response));
        };
    }

    public RouterFunction<ServerResponse> build() {
        return router;
    }
}
