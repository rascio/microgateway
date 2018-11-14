package test.rubrica.server;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import it.r.microgateway.server.url.MessageRouterBuilder;
import it.r.ports.api.DefaultGateway;
import it.r.ports.api.Gateway;
import it.r.ports.api.None;
import it.r.ports.api.Request;
import it.r.ports.hypermedia.api.AuthorizationGateway;
import it.r.ports.hypermedia.jackson.RequestSerializer;
import it.r.ports.utils.AuditGateway;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import test.rubrica.api.CreaPersona;
import test.rubrica.api.DettaglioPersona;
import test.rubrica.api.Persona;
import test.rubrica.api.RubricaRestApi;

import java.io.IOException;

@EnableAutoConfiguration
@SpringBootConfiguration
public class RubricaApplication {

    public static void main(String[] args) {
        SpringApplication.run(RubricaApplication.class, args)
            .getBean(Gateway.class)
            .send(new CreaPersona(new Persona("Paolino", "Paperino", 10)));
    }

    @Bean
    public Gateway gateway() {
        return DefaultGateway.builder()
            .module(new RubricaModule())
            .with(AuditGateway::new)
            .with(AuthorizationGateway.builder()
                .authorize(DettaglioPersona.class, (req, g) -> false)
                .create()
            )
            .build();
    }

    @Bean
    public RouterFunction<ServerResponse> routes(ConversionService conversionService, Gateway gateway) {
        return MessageRouterBuilder.create(
            RubricaRestApi.REGISTRY,
            gateway,
            conversionService
        );
    }

    @Bean
    public ObjectMapper objectMapper() {
        return Jackson2ObjectMapperBuilder.json()
            .serializerByType(None.class, new JsonSerializer<None>() {
                @Override
                public void serialize(it.r.ports.api.None none, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                    jsonGenerator.writeNull();
                }
            })
            .deserializerByType(None.class, new JsonDeserializer<None>() {
                @Override
                public it.r.ports.api.None deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
                    return it.r.ports.api.None.INSTANCE;
                }
            })
            .serializerByType(Request.class, new RequestSerializer(RubricaRestApi.REGISTRY))
            .build();
    }
}
