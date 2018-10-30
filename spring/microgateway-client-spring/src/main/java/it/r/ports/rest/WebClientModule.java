package it.r.ports.rest;

import com.google.common.collect.ImmutableMap;
import it.r.ports.api.DefaultGateway.GatewayWrapper;
import it.r.ports.api.DefaultGateway.Module;
import it.r.ports.api.DefaultGateway.Registry;
import it.r.ports.api.Gateway;
import it.r.ports.api.Request;
import it.r.ports.rest.api.Http;
import it.r.ports.rest.api.RestApiRegistry;
import lombok.RequiredArgsConstructor;
import org.apache.commons.beanutils.BeanMap;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class WebClientModule implements Module {

    private final WebClient webClient;
    private final ConversionService conversionService;
    private final RestApiRegistry restApiRegistry;

    @Override
    public void register(Registry registry, Gateway gateway) {
        restApiRegistry.getApis()
            .forEach((type, http) -> {
                final Function<Request, RequestHeadersSpec<?>> handler;

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
                register(registry, (Class) type, handler);
            });
    }

    private <T extends Request, R> void register(Registry registry, Class<T> type, Function<T, RequestHeadersSpec<?>> handler) {
        registry.register(type, message -> handler.apply(message)
            .exchange()
            .flatMap(response -> response.bodyToMono(message.responseType()))
            .block());
    }

    private <T, B> RequestHeadersSpec<?> post(Http http, Request<?, ?, B, T> message) {
        return webClient.post()
            .uri(builder -> builder.path(http.getPath())
//                .queryParams(toMultiValueMap(message.getParameters()))
                .build(idToMap(message))
            )
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromObject(message.getBody()));
    }

    private <T> RequestHeadersSpec<?> get(Http http, Request<?, ?, ?, T> message) {
        return webClient.get()
            .uri(builder -> builder.path(http.getPath())
                .queryParams(toMultiValueMap(message.getParameters()))
                .build(idToMap(message))
            );
    }

    private MultiValueMap<String, String> toMultiValueMap(Object parameters) {
        if (parameters == null) {
            return new LinkedMultiValueMap<>();
        }
        else if (parameters instanceof MultiValueMap) {
            return (MultiValueMap<String, String>) parameters;
        }
        else if (parameters instanceof Map) {
            final MultiValueMap<String, String> result = new LinkedMultiValueMap<>();
            ((Map) parameters).forEach((k, v) -> result.put(k.toString(), listForParameter(v)));
            return result;
        }
        else {
            return toMultiValueMap(beanMap(parameters));

        }
    }

    private List<String> listForParameter(Object v) {
        final List<String> values;
        if (v instanceof List) {
            values = ((List<?>) v)
                .stream()
                .map(this::toStringEncoded)
                .collect(Collectors.toList());
        } else {
            values = Collections.singletonList(toStringEncoded(v));
        }
        return values;
    }

    private String toStringEncoded(Object v) {
        try {
            return URLEncoder.encode(conversionService.convert(v, String.class), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Object> idToMap(Request req) {
        final Object id = req.getId();
        if (id == null){
            return Collections.emptyMap();
        }
        else if (id instanceof Map) {
            return (Map<String, Object>) id;
        }
        else {
            return ImmutableMap.of("id", id);
        }
    }

    private Map<String, Object> beanMap(Object parameters) {
        final BeanMap map = new BeanMap(parameters);
        final Map<String, Object> res = new HashMap(map);
        res.remove("class");

        return res;
    }
}