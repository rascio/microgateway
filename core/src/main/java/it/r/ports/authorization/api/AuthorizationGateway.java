package it.r.ports.authorization.api;

import it.r.ports.api.Envelope;
import it.r.ports.api.Gateway;
import it.r.ports.api.Request;


public class AuthorizationGateway implements Gateway {

    private final Gateway gateway;

    private AuthorizationGateway(Gateway gateway) {
        this.gateway = gateway;
    }

    @Override
    public <R> R send(Envelope<? extends Request<?, ?, ?, R>> message) {
        if (checkAuth(message)) {
            return gateway.send(message);
        }
        else {
            throw new RuntimeException("Not authorized!");
        }
    }

    private <R, T extends Request<?, ?, ?, R>> boolean checkAuth(Envelope<T> message) {
        return true;
    }

    public static class Builder {

    }
}
