package test;

import it.r.microgateway.server.url.MessageRouterBuilder;
import it.r.ports.api.DefaultGateway;
import it.r.ports.api.Gateway;
import it.r.ports.rest.WebClientAdapter;
import it.r.ports.utils.AuditGateway;
import lombok.SneakyThrows;
import lombok.Value;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.http.server.reactive.ServletHttpHandlerAdapter;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import test.rubrica.RubricaModule;
import test.rubrica.api.*;
import test.rubrica.api.RicercaPersona.PersonaParameters;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Test
public class MainTest {

    private static final DefaultConversionService CONVERSION_SERVICE = new DefaultConversionService();
    private static Tomcat TOMCAT;

    /** /
    public static void main(String[] args) throws Exception {
     Executor executor = Executors.newCachedThreadPool();

     executor.execute(MainTest::server);

     Thread.sleep(1000);

     executor.execute(MainTest::client);
    }
    //*/


    @BeforeClass
    public static void setUp() {
        TOMCAT = MainTest.server();
    }

    @AfterClass
    public static void tearDown() throws LifecycleException {
        TOMCAT.stop();
    }

    @Test
    public static void client() {
        final Gateway port = new WebClientAdapter(
            WebClient.builder()
                .baseUrl("http://localhost:8180")
                .build(),
            CONVERSION_SERVICE,
            RubricaRestApi.REGISTRY);

        System.out.println(port.send(
            new CreaPersona(
                null,
                new Persona(
                    "Paolino",
                    "Paperino",
                    10)
                )
            )
        );
        System.out.println(port.send(
            new CreaPersona(
                null,
                new Persona(
                    "//",
                    "Topolino",
                    10)
                )
            )
        );
        System.out.println(port.send(
            new CreaPersona(
                null,
                new Persona(
                    "Homer",
                    "Simpson",
                    10)
                )
            )
        );

        System.out.println(port.send(
            new RicercaPersona(
                new PersonaParameters(
                    "ino"
                )
            )
        ));

    }

    @SneakyThrows
    public static Tomcat server() {
        final Gateway gateway = new AuditGateway(
            DefaultGateway.from(
                new RubricaModule()
            )
        );

        final RouterFunction<ServerResponse> httpHandler = MessageRouterBuilder.create(
            RubricaRestApi.REGISTRY,
            gateway,
            CONVERSION_SERVICE
        );

        final Tomcat tomcatServer = new Tomcat();
        tomcatServer.setHostname("localhost");
        tomcatServer.setPort(8180);
        final Context rootContext = tomcatServer.addContext("", System.getProperty("java.io.tmpdir"));
        final ServletHttpHandlerAdapter servlet = new ServletHttpHandlerAdapter(
            RouterFunctions.toHttpHandler(httpHandler)
        );
        Tomcat.addServlet(rootContext, "httpHandlerServlet", servlet)
            .setAsyncSupported(true);


        rootContext.addServletMappingDecoded("/", "httpHandlerServlet");

        tomcatServer.start();

        return tomcatServer;

    }
    @Value
    public static class DettaglioPersona {
        private final Persona persona;
        private final AggiornaPersona modifica;
        private final RicercaPersona ricerca;
    }

//    @JsonFilter("hypermedia")
    interface MessageMixin {

    }

    public static void hypermedia() {
        //        final ObjectMapper mapper = new ObjectMapper();
//
//        mapper.setFilterProvider(new SimpleFilterProvider()
//            .addFilter("hypermedia", new PropertyFilter() {
//                @Override
//                public void serializeAsField(Object o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider, PropertyWriter propertyWriter) throws Exception {
//                    final PropertyDescriptor descriptor = Stream.of(Introspector.getBeanInfo(o.getClass()).getPropertyDescriptors())
//                        .filter(pd -> pd.getName().equals(propertyWriter.getName()))
//                        .findFirst()
//                        .get();
//
//
//                    if (!descriptor.getReadMethod().getReturnType().isAssignableFrom(AggiornaPersona.class)) {
//                        propertyWriter.serializeAsField(o, jsonGenerator, serializerProvider);
//                    }
//                }
//
//                @Override
//                public void serializeAsElement(Object o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider, PropertyWriter propertyWriter) throws Exception {
//                    final PropertyDescriptor descriptor = Stream.of(Introspector.getBeanInfo(o.getClass()).getPropertyDescriptors())
//                        .peek(d -> System.out.println(propertyWriter.getName() + " d = " + d.getName()))
//                        .filter(pd -> pd.getName().equals(propertyWriter.getName()))
//                        .findFirst()
//                        .get();
//
//
//                    if (!descriptor.getReadMethod().getReturnType().isAssignableFrom(AggiornaPersona.class)) {
//                        propertyWriter.serializeAsElement(o, jsonGenerator, serializerProvider);
//                    }
//                }
//
//                @Override
//                public void depositSchemaProperty(PropertyWriter propertyWriter, ObjectNode objectNode, SerializerProvider serializerProvider) throws JsonMappingException {
//                    propertyWriter.depositSchemaProperty(objectNode, serializerProvider);
//                }
//
//                @Override
//                public void depositSchemaProperty(PropertyWriter propertyWriter, JsonObjectFormatVisitor jsonObjectFormatVisitor, SerializerProvider serializerProvider) throws JsonMappingException {
//                    propertyWriter.depositSchemaProperty(jsonObjectFormatVisitor, serializerProvider);
//                }
//            }));
//
//        mapper.registerModule(new SimpleModule()
//            .addSerializer(Message.class, new JsonSerializer<Message>() {
//                @Override
//                public void serialize(Message message, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
//
//                    jsonGenerator.writeString(message.getClass().getAnnotation(Http.class).path());
//                }
//            })
//        );
//        mapper.addMixIn(Object.class, MessageMixin.class);
//
//        System.out.println(mapper.writeValueAsString(
//            new DettaglioPersona(
//                new Persona("Paolino", "Paperino", null),
//                new AggiornaPersona("XXX", null),
//                new RicercaPersona("YYY", "")
//            )
//        ));
    }
}
