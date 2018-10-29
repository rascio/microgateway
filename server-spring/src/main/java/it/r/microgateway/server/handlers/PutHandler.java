package it.r.microgateway.server.handlers;

import com.google.common.collect.ImmutableMap;
import it.r.ports.api.Gateway;
import it.r.ports.api.Request;
import it.r.ports.utils.BeanUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.BodyInserters.fromObject;

public class PutHandler extends AbstractHandler {

    public PutHandler(Class<? extends Request> type, Gateway gateway, ConversionService conversionService) {
        super(type, gateway, conversionService);
    }

    @Override
    public Mono<ServerResponse> handle(ServerRequest request) {
        return request.bodyToMono(typeOfField(type, "body"))
            .map(body -> {
                final Request r = BeanUtils.newInstance(type, ImmutableMap.of(
                    "body", body,
                    "id", convertId(request, typeOfField(type, "id"))
//                        "parameters", conversionService.convert(request.queryParams(), typeOfField(type, "parameters"))
                ));
                return r;
            })
            .flatMap(msg -> Mono.justOrEmpty(gateway.send(msg)))
            .flatMap(resp -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(fromObject(resp))
            )
            .switchIfEmpty(ServerResponse.notFound()
                .build()
            );
    }

}
