package it.r.ports.authorization.api;

import it.r.ports.api.Gateway;
import it.r.ports.api.Request;


public class AuthorizationGateway implements Gateway {

    private final Gateway gateway;

    private AuthorizationGateway(Gateway gateway) {
        this.gateway = gateway;
    }

    @Override
    public <I, P, B, T> T send(Request<I, P, B, T> message) {
        return null;
    }

    public static class Builder {

    }
}
