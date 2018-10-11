package it.r.microgateway.server.url;

import com.google.common.collect.ImmutableMap;
import it.r.ports.api.Message;
import it.r.ports.rest.api.Http;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class SpringMessageMappingRegistry {

    private static final SpringMessageMappingRegistry EMPTY = new SpringMessageMappingRegistry(ImmutableMap.of());

    public static SpringMessageMappingRegistry create() {
        return new SpringMessageMappingRegistry(new HashMap<>());
    }

    public static SpringMessageMappingRegistry empty() {
        return EMPTY;
    }

    private final Map<Http, SpringMessageMapping<? extends Message<?>>> apis;

    private SpringMessageMappingRegistry(Map<Http, SpringMessageMapping<? extends Message<?>>> apis) {
        this.apis = apis;
    }

    public void add(Http http, SpringMessageMapping<? extends Message<?>> api) {
        this.apis.put(http, api);
    }

    public Optional<SpringMessageMapping<?>> findByUri(String uri) {
        return apis.values()
            .stream()
            .sorted()
            .filter(api -> api.matches(uri))
            .findFirst();
    }

    public Optional<SpringMessageMapping<? extends Message<?>>> findByMessage(Message<?> message) {
        return Optional.ofNullable(apis.get(message.getClass().getAnnotation(Http.class)));
    }
}
