package it.r.ports.api;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DefaultGateway implements Gateway {

    private final Map<Class<?>, Function<Request<?, ?, ?, ?>, ?>> handlers;

    public static Builder builder() {
        return new Builder();
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
        void register(Registry registry, Gateway gateway);
    }

    static class LifecycleGateway implements Gateway {

        private Gateway gateway;

        @Override
        public <I, P, B, T> T send(Request<I, P, B, T> message) {
            Preconditions.checkState(gateway != null, "Gateway not started yet");
            return gateway.send(message);
        }

        public void start(Gateway gateway) {
            Preconditions.checkState(this.gateway == null, "Gateway already started");
            this.gateway = gateway;
        }
    }

    public interface GatewayWrapper {
        Gateway wrap(Gateway gateway);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder {
        private final List<Module> modules = new LinkedList<>();
        private final List<GatewayWrapper> wrappers = new LinkedList<>();

        public Builder with(GatewayWrapper wrapper) {
            this.wrappers.add(wrapper);
            return this;
        }

        public Builder module(Module module) {
            this.modules.add(module);
            return this;
        }

        public Gateway build() {
            final Registry registry = new Registry();
            final LifecycleGateway gateway = new LifecycleGateway();
            modules.forEach(m -> m.register(registry, gateway));
            gateway.start(new DefaultGateway(registry.handlers));

            Gateway result = gateway;
            for (GatewayWrapper wrapper : wrappers) {
                result = wrapper.wrap(result);
            }
            return result;
        }
    }
}
