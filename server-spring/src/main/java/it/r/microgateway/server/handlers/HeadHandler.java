package it.r.microgateway.server.handlers;

import com.google.common.collect.ImmutableMap;
import it.r.ports.api.Gateway;
import it.r.ports.api.Request;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static it.r.ports.utils.BeanUtils.newInstance;
import static org.springframework.web.reactive.function.BodyInserters.fromObject;

public class HeadHandler extends AbstractHandler {

    public HeadHandler(Class<? extends Request> type, Gateway gateway, ConversionService conversionService) {
        super(type, gateway, conversionService);
    }

    @Override
    public Mono<ServerResponse> handle(ServerRequest request) {
        final Request r = newInstance(type, ImmutableMap.of(
            "id", convertId(request, typeOfField(type, "id")),
            "parameters", newInstance(typeOfField(type, "parameters"),
                    toSimpleMap(request.queryParams())
                )
        ));

        gateway.send(r);

        return ServerResponse.ok()
            .build();
    }

}
