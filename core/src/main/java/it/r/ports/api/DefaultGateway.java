package it.r.ports.api;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public class DefaultGateway implements Gateway {

    private final Map<Class<?>, Function<Request<?, ?, ?, ?>, ?>> handlers;

    public static Gateway from(Module...modules) {
        final Registry registry = new Registry();
        Stream.of(modules)
            .forEach(m -> m.register(registry));
        return new DefaultGateway(registry.handlers);
    }

    private DefaultGateway(Map<Class<?>, Function<Request<?, ?, ?, ?>, ?>> handlers) {
        this.handlers = handlers;
    }

    @Override
    public <I, P, B, T> T send(Request<I, P, B, T> message) {

        final Function<Request<?, ?, ?, ?>, ?> handler = handlers.get(message.getClass());
        return (T) handler.apply(message);
    }

    public static class Registry {
        private final Map<Class<?>, Function<Request<?, ?, ?, ?>, ?>> handlers = new HashMap<>();

        public <T extends Request<?, ?, ?, R>, R> Registry register(Class<T> type, Function<T, R> handler) {
            this.handlers.put(type, (Function<Request<?, ?, ?, ?>, ?>) handler);
            return this;
        }
    }

    public interface Module {
        void register(Registry registry);
    }
}
