package it.r.ports.api;

public interface StaticCommand<B, R> extends Command<None, B, R> {

    @Override
    default None getId() {
        return None.INSTANCE;
    }
}
