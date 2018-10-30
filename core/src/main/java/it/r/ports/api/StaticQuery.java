package it.r.ports.api;

public interface StaticQuery<P, R> extends Query<None, P, R>{

    @Override
    default None getId() {
        return None.INSTANCE;
    }
}
