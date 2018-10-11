package it.r.microgateway.server.gateway;

import it.r.ports.api.Gateway;
import it.r.ports.api.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public class DefaultGateway implements Gateway {

    private final Map<Class<?>, Function<Message<?>, ?>> handlers;

    public static Gateway from(Module...modules) {
        final Registry registry = new Registry();
        Stream.of(modules)
            .forEach(m -> m.register(registry));
        return new DefaultGateway(registry.handlers);
    }

    private DefaultGateway(Map<Class<?>, Function<Message<?>, ?>> handlers) {
        this.handlers = handlers;
    }

    @Override
    public <T> T send(Message<T> message) {

        final Function<Message<?>, ?> handler = handlers.get(message.getClass());
        return (T) handler.apply(message);
    }

    public static class Registry {
        private final Map<Class<?>, Function<Message<?>, ?>> handlers = new HashMap<>();

        public <T extends Message<R>, R> void register(Class<T> type, Function<T, R> handler) {
            this.handlers.put(type, (Function<Message<?>, ?>) handler);
        }
    }

    public interface Module {
        void register(Registry registry);
    }
}
