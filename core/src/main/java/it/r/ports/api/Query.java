package it.r.ports.api;

public interface Query<I, P, R> extends Request<I, P, Void, R>{

    default Void getBody() {
        return null;
    }
}
