package it.r.ports.api;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Function;

import static org.slf4j.LoggerFactory.getLogger;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DefaultGateway implements Gateway {

    public static final Logger LOGGER = getLogger(DefaultGateway.class);

    private final Map<Class<?>, Function<Envelope<?>, ?>> handlers;

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public <R> R send(Envelope<? extends Request<?, ?, ?, R>> message) {
        final Function<Envelope<? extends Request<?, ?, ?, R>>, R> handler = handlerFor(message);
        return handler.apply(message);
    }

    private <R> Function<Envelope<? extends Request<?, ?, ?, R>>, R> handlerFor(Envelope<? extends Request<?, ?, ?, R>> message) {
        //Fuck the (type) system
        final Class<?> type = message.getRequest().getClass();
        final Function handler = handlers.get(type);

        LOGGER.trace("handlerFor({}): {}", type.getName(), handler);

        return handler;
    }
    static final int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

    public static class Registry {
        private final Map<Class<?>, Function<Envelope<?>, ?>> handlers = new HashMap<>();

        public <T extends Request<?, ?, ?, R>, R> Registry register(Class<T> type, Function<Envelope<T>, R> handler) {
            Preconditions.checkArgument(handler != null);
            //Fuck the (type) system!
            final Function fn = handler;
            this.handlers.put(type, fn);
            return this;
        }
    }

    public interface Module {
        void register(Registry registry, Gateway gateway);
    }

    static class LifecycleGateway implements Gateway {

        private Gateway gateway;

        @Override
        public <R> R send(Envelope<? extends Request<?, ?, ?, R>> message) {
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

            Gateway result = new DefaultGateway(registry.handlers);
            for (GatewayWrapper wrapper : wrappers) {
                result = wrapper.wrap(result);
            }

            gateway.start(result);


            return result;
        }
    }
}
