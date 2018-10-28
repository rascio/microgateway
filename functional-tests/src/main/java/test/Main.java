package test;

import it.r.microgateway.server.gateway.DefaultGateway;
import it.r.microgateway.server.url.MessageRouterBuilder;
import it.r.ports.api.Gateway;
import it.r.ports.rest.WebClientAdapter;
import it.r.ports.rest.api.Http;
import it.r.ports.rest.api.HttpMethod;
import it.r.ports.rest.api.RestApiRegistry;
import lombok.SneakyThrows;
import lombok.Value;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.http.server.reactive.ServletHttpHandlerAdapter;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import test.RicercaPersona.PersonaParameters;

import java.time.LocalDate;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Main {

    private static final RestApiRegistry REGISTRY = RestApiRegistry.builder()
        .query(RicercaPersona.class, new Http(HttpMethod.GET, "/{id}/ricerca"))
        .command(AggiornaPersona.class, new Http(HttpMethod.POST, "/persona/{id}"))
        .build();

    private static final DefaultConversionService CONVERSION_SERVICE = new DefaultConversionService();

    public static void main(String[] args) throws Exception {

        Executor executor = Executors.newCachedThreadPool();

        executor.execute(Main::server);

        Thread.sleep(1000);

        executor.execute(Main::client);

    }

    public static void client() {
        final Gateway port = new WebClientAdapter(
            WebClient.builder()
                .baseUrl("http://localhost:8180")
                .build(),
            CONVERSION_SERVICE,
            REGISTRY);

        System.out.println(port.send(new RicercaPersona("xxx", new PersonaParameters("nome:Manuel AND cognome:Rasc*"))));;
        System.out.println(port.send(new AggiornaPersona("aa00", new Persona("asd", "dsa", 10))));;

    }

    @SneakyThrows
    public static void server() {
        final Gateway gateway = DefaultGateway.from(
            registry -> registry.register(
                RicercaPersona.class, RicercaPersona::toString
            )
            .register(AggiornaPersona.class, cmd -> {
                System.out.println("cmd = " + cmd);
                return null;
            })
        );

        final RouterFunction<ServerResponse> httpHandler = MessageRouterBuilder.create(REGISTRY, gateway, CONVERSION_SERVICE);

        System.out.println("httpHandler = " + httpHandler);

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
        Thread.sleep(10000000);
        tomcatServer.stop();

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
