package it.r.microgateway.server.controller;

import it.r.microgateway.server.url.RequestUrlMapper;
import it.r.ports.api.Gateway;
import it.r.ports.api.Message;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Optional;

public class GatewayController {

    private final RequestUrlMapper requestUrlMapper;
    private final Gateway gateway;

    public GatewayController(RequestUrlMapper requestUrlMapper, Gateway gateway) {
        this.requestUrlMapper = requestUrlMapper;
        this.gateway = gateway;
    }

    @RequestMapping
    public ResponseEntity<?> handle(HttpServletRequest request) throws IOException {
        final Message<?> message = requestUrlMapper.resolve(request);

        Object response = gateway.send(message);

        if (response instanceof Optional) {
            Optional<?> responseOpt = (Optional<?>) response;
            if (!responseOpt.isPresent()) {
                return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
            }
            response = responseOpt.get();
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
