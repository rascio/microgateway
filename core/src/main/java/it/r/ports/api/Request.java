package it.r.ports.api;

public interface Request<I, P, B, R> {
    I getId();
    P getParameters();
    B getBody();

    Class<R> responseType();
}
