package it.r.microgateway.server.url;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.r.ports.api.Message;
import it.r.ports.rest.api.Http;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

public class SpringRequestUrlMapper implements RequestUrlMapper {

    private static final String GET = "GET";
    private static final String POST = "POST";

    private static final Logger logger = LoggerFactory.getLogger(SpringRequestUrlMapper.class);

    private final ObjectMapper mapper;
    private final String domain;
    private final DiskFileItemFactory factory;

    private final SpringMessageMappingRegistry registry = SpringMessageMappingRegistry.create();

    public SpringRequestUrlMapper(ObjectMapper mapper, String domain) {
        this.mapper = mapper;
        this.domain = domain;
        try {
            this.factory = new DiskFileItemFactory(10240, Files.createTempDirectory("MessageUrlMapper").toFile());
        }
        catch (IOException e) {
            throw new RuntimeException("Error creating temporary folder", e);
        }
    }

    @Override
    public String resolve(Message<?> request) {
        final Optional<SpringMessageMapping<?>> api = registry.findByMessage(request);

        if (!api.isPresent()) {
            throw new RuntimeException("Can't find a registered API for: " + request);
        }
        return api.get().link(request);
    }

    @Override
    public Message<?> resolve(HttpServletRequest request) throws IOException {
        logger.debug("[{}] {}", request.getMethod(), request.getRequestURI());

        final Optional<SpringMessageMapping<?>> api = registry.findByUri(request.getRequestURI());

        if (!api.isPresent()) {
            throw new IllegalArgumentException(String.format("No api mapping found for uri %s %s", request.getMethod(), request.getRequestURI()));
        }

        return api.get().createMessage(request);
    }

    @Override
    public <C extends Message<?>> void register(Class<C> type) {
        final Http http = type.getAnnotation(Http.class);
        registry.add(http, new SpringGetRequestMapping<>(domain, http.path(), type));
    }

    @Override
    public String toString() {
        return "SpringRequestUrlMapper{" +
            "domain='" + domain + '\'' +
            ", apis=" + registry +
            '}';
    }
}
