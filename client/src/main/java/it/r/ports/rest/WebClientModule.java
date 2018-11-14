package it.r.ports.rest;

import it.r.ports.api.DefaultGateway.Module;
import it.r.ports.api.DefaultGateway.Registry;
import it.r.ports.api.Envelope;
import it.r.ports.api.Gateway;
import it.r.ports.api.StaticQuery;
import it.r.ports.rest.api.Http;
import it.r.ports.rest.api.RestApiRegistry;
import it.r.ports.utils.UriUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.Function;

@RequiredArgsConstructor
public class WebClientModule implements Module {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebClientModule.class);

    private final WebClient webClient;
    private final RestApiRegistry restApiRegistry;

    @Override
    public void register(Registry registry, Gateway gateway) {
        restApiRegistry.getApis()
            .forEach((type, http) -> {
                final Function<Envelope, RequestHeadersSpec<?>> handler;

                switch (http.getMethod()) {
                    case GET:
                        handler = message -> get(http, message);
                        break;
                    case POST:
                        handler = message -> post(http, message);
                        break;
                    default:
                        throw new IllegalArgumentException("Not managed " + http.getMethod() + " for " + type.getName());
                }
                register(registry, type, handler);
            });
    }

    private <T> void register(Registry registry, Class type, Function<Envelope, RequestHeadersSpec<?>> handler) {
        LOGGER.debug("Registration handler for {}", type.getName());
        registry.register(type, message -> handler.apply(message)
            .exchange()
            .flatMap(response -> response.bodyToMono(message.getRequest().responseType()))
            .doOnNext(v -> LOGGER.info(v.getClass().getName()))
            .block());
    }

    private RequestHeadersSpec<?> post(Http http, Envelope message) {
        return webClient.post()
            .uri(builder -> UriUtils.toURI(message.getRequest(), http, builder)
            )
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromObject(message.getRequest().getBody()));
    }

    private RequestHeadersSpec<?> get(Http http, Envelope message) {
        return webClient.get()
            .uri(builder -> UriUtils.toURI(message.getRequest(), http, builder)
            );
    }

}
