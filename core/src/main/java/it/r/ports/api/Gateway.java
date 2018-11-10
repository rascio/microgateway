package it.r.ports.api;

import org.springframework.http.HttpHeaders;

public interface Gateway {

    default <I, P, B, T> T send(Request<I, P, B, T> message) {
        return send(new Envelope<>(new HttpHeaders(), message, null));
    }

    <R> R send(Envelope<? extends Request<?, ?, ?, R>> message);
}
