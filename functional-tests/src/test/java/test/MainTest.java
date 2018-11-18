package test;

import it.r.ports.api.DefaultGateway;
import it.r.ports.api.Gateway;
import it.r.ports.api.Request;
import it.r.ports.hypermedia.jackson.RequestDeserializer;
import it.r.ports.rest.WebClientModule;
import it.r.ports.utils.AuditGateway;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
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
                    .clientConnector(new ReactorClientHttpConnector())
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

        try {
            port.send(new DettaglioPersona(homer));
        }
        catch (Exception e) {
            System.out.println("e.getMessage() = " + e.getMessage());
        }


    }

    private static ExchangeStrategies exchangeStrategy() {
        return ExchangeStrategies.builder()
            .codecs(conf -> conf.defaultCodecs()
                .jackson2JsonDecoder(
                    new Jackson2JsonDecoder(
                        Jackson2ObjectMapperBuilder.json()
                            .deserializerByType(Request.class, new RequestDeserializer(null))
                        .build()
                    )
                )
            )
            .build();
    }

}
