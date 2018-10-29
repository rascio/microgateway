package it.r.ports.api;

public interface Command<I, B, R> extends Request<I, None, B, R> {

    default None getParameters() {
        return None.INSTANCE;
    }

}
