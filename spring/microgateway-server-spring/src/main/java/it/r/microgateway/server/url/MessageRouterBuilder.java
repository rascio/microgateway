package it.r.microgateway.server.url;

import it.r.microgateway.server.handlers.GetHandler;
import it.r.microgateway.server.handlers.HeadHandler;
import it.r.microgateway.server.handlers.PostHandler;
import it.r.microgateway.server.handlers.PutHandler;
import it.r.ports.api.Gateway;
import it.r.ports.api.Request;
import it.r.ports.rest.api.Http;
import it.r.ports.rest.api.HttpMethod;
import it.r.ports.rest.api.RestApiRegistry;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

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

        private RouterFunction<ServerResponse> handlerFor(Class<? extends Request<?, ?, ?, ?>> type, Gateway gateway, Http http) {

            LOGGER.info("Registering handler for {}", type.getName());

            final RouterFunction<ServerResponse> route;
            switch (http.getMethod()) {
                case GET:
                    route = RouterFunctions.route(GET(http.getPath()), new GetHandler(type, gateway, conversionService));
                    break;
                case POST:
                    route = RouterFunctions.route(POST(http.getPath()), new PostHandler(type, gateway, conversionService));
                    break;
                case PUT:
                    route = RouterFunctions.route(PUT(http.getPath()), new PutHandler(type, gateway, conversionService));
                    break;
                case HEAD:
                    route = RouterFunctions.route(HEAD(http.getPath()), new HeadHandler(type, gateway, conversionService));
                    break;
                default:
                    throw new IllegalStateException("What? " + type.getName());
            }

            return route;

        }


    }
}
