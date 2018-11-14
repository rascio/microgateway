package it.r.ports.hypermedia.api;

import it.r.ports.api.Envelope;
import it.r.ports.api.Gateway;
import it.r.ports.api.Request;

public interface RequestFilter<T extends Request<?, ?, ?, ?>> {

    boolean authorized(Envelope<T> request, Gateway gateway);
}
