package it.r.ports.hypermedia.api;

import it.r.ports.api.Envelope;

public class AuthorizationException extends RuntimeException {

    private Envelope<?> envelope;

    public AuthorizationException(Envelope <?> envelope, String message) {
        super(message);
        this.envelope = envelope;
    }

    public Envelope<?> getEnvelope() {
        return envelope;
    }
}
