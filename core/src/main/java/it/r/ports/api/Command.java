package it.r.ports.api;

public interface Command<I, B, R> extends Request<I, Void, B, R> {

    default Void getParameters() {
        return null;
    }
}
