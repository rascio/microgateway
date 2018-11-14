package test;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import it.r.ports.api.DefaultGateway;
import it.r.ports.api.Gateway;
import it.r.ports.api.Request;
import it.r.ports.rest.WebClientModule;
import it.r.ports.utils.AuditGateway;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import test.rubrica.api.*;
import test.rubrica.api.RicercaPersona.PersonaParameters;
import test.rubrica.api.RicercaPersona.PersonaResult;
import test.rubrica.server.RubricaApplication;

import java.io.IOException;
import java.util.List;

public class MainTest {

    private static ConfigurableApplicationContext APPLICATION;

    @BeforeClass
    public static void setUp() {
        APPLICATION = SpringApplication.run(RubricaApplication.class);
    }

    @AfterClass
    public static void tearDown() {
        APPLICATION.stop();
    }

    @Test
    public static void client() {
        final Gateway port = DefaultGateway.builder()
            .module(new WebClientModule(
                WebClient.builder()
                    .baseUrl("http://localhost:8080")
                    .exchangeStrategies(
                        exchangeStrategy()
                    )
                    .build(),
                RubricaRestApi.REGISTRY
            ))
            .with(AuditGateway::new)
            .build();

        System.out.println(port.send(
            new CreaPersona(
                new Persona(
                    "Paolino",
                    "Paperino",
                    10)
                )
            )
        );
        System.out.println(port.send(
            new CreaPersona(
                new Persona(
                    "//",
                    "Topolino",
                    10)
                )
            )
        );
        final String homer = port.send(
            new CreaPersona(
                new Persona(
                    "Homer",
                    "Simpson",
                    10)
            )
        );
        System.out.println(homer);

        final List<PersonaResult> persons = port.send(
            new RicercaPersona(
                new PersonaParameters(
                    "ino"
                )
            )
        );
        Assert.assertEquals(persons.size(), 2);
        persons.forEach(p -> System.out.println("p.getCognome() = " + p.getCognome()));

        final Persona persona = port.send(new DettaglioPersona(homer));

        System.out.println("homer = " + persona);

    }

    private static ExchangeStrategies exchangeStrategy() {
        return ExchangeStrategies.builder()
            .codecs(conf -> conf.defaultCodecs()
                .jackson2JsonDecoder(
                    new Jackson2JsonDecoder(
                        Jackson2ObjectMapperBuilder.json()
                            .deserializerByType(Request.class, new JsonDeserializer<Request>() {
                                @Override
                                public Request deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
                                    //It's a kind of magic
                                    jsonParser.nextFieldName();
                                    jsonParser.nextToken();
                                    final String ref = jsonParser.getValueAsString();
                                    jsonParser.nextToken();
                                    return new Request() {
                                        @Override
                                        public Object getId() {
                                            return ref;
                                        }

                                        @Override
                                        public Object getParameters() {
                                            return null;
                                        }

                                        @Override
                                        public Object getBody() {
                                            return null;
                                        }

                                        @Override
                                        public String toString() {
                                            return "Request["+ref+"]";
                                        }
                                    };
                                }
                            })
                        .build()
                    )
                )
            )
            .build();
    }

}
