package it.r.ports.rest.api;

import it.r.ports.api.Command;
import it.r.ports.api.Query;
import it.r.ports.api.Request;
import lombok.Value;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

@Value
public class RestApiRegistry {

    private final Map<Class<? extends Request<?, ?, ?, ?>>, Http> apis;

    public Optional<Http> find(Class<? extends Request> api) {
        return Optional.ofNullable(apis.get(api));
    }

    public static RestApiRegistry create(Consumer<Register> factory) {
        final Register register = new Register();
        factory.accept(register);
        return new RestApiRegistry(register.apis);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Map<Class<? extends Request<?, ?, ?, ?>>, Http> apis = new HashMap<>();
        public <Q extends Query<?, ?, ?>> Builder get(Class<Q> type, String path) {
            apis.put(type, new Http(HttpMethod.GET, path));
            return this;
        }
        public <C extends Command<?, ?, ?>> Builder post(Class<C> type, String path) {
            apis.put(type, new Http(HttpMethod.POST, path));
            return this;
        }
        public <C extends Command<?, ?, ?>> Builder put(Class<C> type, String path) {
            apis.put(type, new Http(HttpMethod.PUT, path));
            return this;
        }
        public <C extends Query<?, ?, Boolean>> Builder head(Class<C> type, String path) {
            apis.put(type, new Http(HttpMethod.HEAD, path));
            return this;
        }
        public RestApiRegistry build() {
            return new RestApiRegistry(apis);
        }
    }

    public static class Register {
        private final Map<Class<? extends Request<?, ?, ?, ?>>, Http> apis = new HashMap<>();
        public <Q extends Query<?, ?, ?>> Register query(Class<Q> type, Http http) {
            apis.put(type, http);
            return this;
        }
        public <C extends Command<?, ?, ?>> Register command(Class<C> type, Http http) {
            apis.put(type, http);
            return this;
        }
    }
}
