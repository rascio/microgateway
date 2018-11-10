package it.r.microgateway.server.handlers;

import it.r.microgateway.server.utils.BeanBuilder;
import it.r.ports.api.Gateway;
import it.r.ports.api.Request;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static it.r.ports.utils.BeanUtils.newInstance;
import static org.springframework.web.reactive.function.BodyInserters.fromObject;

public class GetHandler extends AbstractHandler {

    public GetHandler(Class<? extends Request<?, ?, ?, ?>> type, Gateway gateway, ConversionService conversionService) {
        super(type, gateway, conversionService);
    }

    @Override
    public Mono<ServerResponse> handle(ServerRequest request) {
        final Request r = new BeanBuilder<>(type)
            .with("id", convertId(request))
            .with("parameters", convertParameters(request))
            .build();

        final Object response = gateway.send(envelope(request, r));

        return ServerResponse.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(fromObject(response));
    }

}
