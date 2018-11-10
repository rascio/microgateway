package it.r.microgateway.server.handlers;

import it.r.microgateway.server.utils.BeanBuilder;
import it.r.ports.api.Gateway;
import it.r.ports.api.Request;
import it.r.ports.utils.BeanUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.BodyInserters.fromObject;

public class PostHandler extends AbstractHandler {

    public PostHandler(Class<? extends Request<?, ?, ?, ?>> type, Gateway gateway, ConversionService conversionService) {
        super(type, gateway, conversionService);
    }

    @Override
    public Mono<ServerResponse> handle(ServerRequest request) {
        return request.bodyToMono(typeOfField(type, "body").get()) //TODO manage properly
            .map(body -> new BeanBuilder<>(type)
                    .with("body", body)
                    .with("id", convertId(request))
//                        "parameters", conversionService.convert(request.queryParams(), typeOfField(type, "parameters"))
                    .build()
            )
            .flatMap(msg -> Mono.justOrEmpty(gateway.send(envelope(request, msg))))
            .flatMap(resp -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(fromObject(resp))
            )
            .switchIfEmpty(ServerResponse.notFound()
                .build()
            );
    }

}
