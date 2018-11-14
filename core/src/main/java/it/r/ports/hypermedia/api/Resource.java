package it.r.ports.hypermedia.api;

import it.r.ports.api.Request;
import lombok.ToString;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@ToString
public abstract class Resource {

    private final Map<String, Request<?, ?, ?, ?>> links = new HashMap<>();

    public Map<String, Request<?, ?, ?, ?>> getLinks() {
        return Collections.unmodifiableMap(links);
    }

    public void link(String ref, Request<?, ?, ?, ?> req) {
        links.put(ref, req);
    }

    void filter(Predicate<Request<?, ?, ?, ?>> filter) {
        links.entrySet()
            .stream()
            .filter(e -> !filter.test(e.getValue()))
            .map(Entry::getKey)
            .collect(Collectors.toList())
            .forEach(links::remove);
    }
}
