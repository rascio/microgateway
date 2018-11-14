package it.r.ports.api;

import it.r.ports.utils.Introspection;
import org.springframework.core.ParameterizedTypeReference;

public interface Request<I, P, B, R> {
    I getId();
    P getParameters();
    B getBody();

    default ParameterizedTypeReference<R> responseType() {
        return Introspection.responseType(this.getClass());
    }


}
