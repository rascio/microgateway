package it.r.ports.api;

import lombok.Value;
import org.springframework.http.HttpHeaders;

import java.security.Principal;

@Value
public class Envelope<T extends Request<?, ?, ?, ?>> {
    HttpHeaders headers;
    T request;
    Principal principal;

    public <R extends Request<?, ?, ?, ?>> Envelope<R> withRequest(R request) {
        return new Envelope<>(this.headers, request, this.principal);
    }
}
