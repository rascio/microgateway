package it.r.ports.api;

import it.r.ports.utils.Introspection;

public interface Request<I, P, B, R> {
    I getId();
    P getParameters();
    B getBody();

    default Class<R> responseType() {
        return Introspection.responseType(this.getClass());
    }
}
