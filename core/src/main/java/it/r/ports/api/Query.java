package it.r.ports.api;

public interface Query<I, P, R> extends Request<I, P, None, R>{

    default None getBody() {
        return None.INSTANCE;
    }
}
