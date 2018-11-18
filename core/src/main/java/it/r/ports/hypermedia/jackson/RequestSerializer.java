package it.r.ports.hypermedia.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import it.r.ports.api.Command;
import it.r.ports.api.Request;
import it.r.ports.rest.api.Http;
import it.r.ports.rest.api.RestApiRegistry;
import it.r.ports.utils.UriUtils;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.io.IOException;
import java.net.URI;

public class RequestSerializer extends JsonSerializer<Request<?, ?, ?, ?>> {
    private final RestApiRegistry registry;

    public RequestSerializer(RestApiRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void serialize(Request<?, ?, ?, ?> request, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        final Http http = registry.find(request.getClass())
            .orElse(null);

        final URI uri = UriUtils.toURI(request, http, new DefaultUriBuilderFactory().builder());

        jsonGenerator.writeStartObject();
        jsonGenerator.writeObjectField(
            "ref", uri
        );
        jsonGenerator.writeObjectField(
            "method", http.getMethod()
        );
        if (request instanceof Command) {
            jsonGenerator.writeObjectField(
                "body",
                request.getBody()
            );
        }
        jsonGenerator.writeEndObject();
    }

}
