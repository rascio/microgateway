package it.r.ports.hypermedia.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.collect.ImmutableMap;
import it.r.ports.api.Request;
import it.r.ports.rest.api.Http;
import it.r.ports.rest.api.RestApiRegistry;
import it.r.ports.utils.UriUtils;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.io.IOException;

public class RequestSerializer extends JsonSerializer<Request<?, ?, ?, ?>> {
    private final RestApiRegistry registry;

    public RequestSerializer(RestApiRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void serialize(Request<?, ?, ?, ?> request, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        final Http http = registry.find(request.getClass())
            .orElse(null);

        jsonGenerator.writeObject(ImmutableMap.of(
            "ref", UriUtils.toURI(request, http, new DefaultUriBuilderFactory().builder())
        ));
    }

}
