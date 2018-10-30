package it.r.microgateway.server.handlers;

import it.r.microgateway.server.utils.BeanBuilder;
import it.r.ports.api.Gateway;
import it.r.ports.api.Request;
import org.springframework.core.convert.ConversionService;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static it.r.ports.utils.BeanUtils.newInstance;

public class HeadHandler extends AbstractHandler {

    public HeadHandler(Class<? extends Request<?, ?, ?, ?>> type, Gateway gateway, ConversionService conversionService) {
        super(type, gateway, conversionService);
    }

    @Override
    public Mono<ServerResponse> handle(ServerRequest request) {
        final Request r = new BeanBuilder<>(type)
            .with("id", convertId(request))
            .with("parameters", convertParameters(request))
            .build();

        gateway.send(r);

        return ServerResponse.ok()
            .build();
    }

}
