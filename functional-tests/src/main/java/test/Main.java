package test;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import it.r.ports.api.CommunicationException;
import it.r.ports.api.Gateway;
import it.r.ports.api.Message;
import it.r.ports.rest.JerseyAdapter;
import it.r.ports.rest.api.Http;
import lombok.Value;

import javax.ws.rs.client.ClientBuilder;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) throws CommunicationException, JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();

        mapper.setFilterProvider(new SimpleFilterProvider()
            .addFilter("hypermedia", new PropertyFilter() {
                @Override
                public void serializeAsField(Object o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider, PropertyWriter propertyWriter) throws Exception {
                    final PropertyDescriptor descriptor = Stream.of(Introspector.getBeanInfo(o.getClass()).getPropertyDescriptors())
                        .filter(pd -> pd.getName().equals(propertyWriter.getName()))
                        .findFirst()
                        .get();


                    if (!descriptor.getReadMethod().getReturnType().isAssignableFrom(AggiornaPersona.class)) {
                        propertyWriter.serializeAsField(o, jsonGenerator, serializerProvider);
                    }
                }

                @Override
                public void serializeAsElement(Object o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider, PropertyWriter propertyWriter) throws Exception {
                    final PropertyDescriptor descriptor = Stream.of(Introspector.getBeanInfo(o.getClass()).getPropertyDescriptors())
                        .peek(d -> System.out.println(propertyWriter.getName() + " d = " + d.getName()))
                        .filter(pd -> pd.getName().equals(propertyWriter.getName()))
                        .findFirst()
                        .get();


                    if (!descriptor.getReadMethod().getReturnType().isAssignableFrom(AggiornaPersona.class)) {
                        propertyWriter.serializeAsElement(o, jsonGenerator, serializerProvider);
                    }
                }

                @Override
                public void depositSchemaProperty(PropertyWriter propertyWriter, ObjectNode objectNode, SerializerProvider serializerProvider) throws JsonMappingException {
                    propertyWriter.depositSchemaProperty(objectNode, serializerProvider);
                }

                @Override
                public void depositSchemaProperty(PropertyWriter propertyWriter, JsonObjectFormatVisitor jsonObjectFormatVisitor, SerializerProvider serializerProvider) throws JsonMappingException {
                    propertyWriter.depositSchemaProperty(jsonObjectFormatVisitor, serializerProvider);
                }
            }));

        mapper.registerModule(new SimpleModule()
            .addSerializer(Message.class, new JsonSerializer<Message>() {
                @Override
                public void serialize(Message message, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {

                    jsonGenerator.writeString(message.getClass().getAnnotation(Http.class).path());
                }
            })
        );
        mapper.addMixIn(Object.class, MessageMixin.class);

        System.out.println(mapper.writeValueAsString(
            new DettaglioPersona(
                new Persona("Paolino", "Paperino", null),
                new AggiornaPersona("XXX", null),
                new RicercaPersona("YYY", "")
            )
        ));

        final Gateway port = new JerseyAdapter(
            ClientBuilder.newClient()
                .target("http://localhost:8080")
        );

//        System.out.println(port.send(new AggiornaPersona("aa00", new Persona("asd", "dsa", LocalDate.now()))));;
//        System.out.println(port.send(new RicercaPersona("aaa-bbb-cc01", "nome:Manuel AND cognome:Rasc*")));;

    }

    @Value
//    @Http()
    public static class DettaglioPersona {
        private final Persona persona;
        private final AggiornaPersona modifica;
        private final RicercaPersona ricerca;
    }

    @JsonFilter("hypermedia")
    interface MessageMixin {

    }
}
