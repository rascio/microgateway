package test;

import it.r.ports.api.CommunicationException;
import it.r.ports.api.Port;
import it.r.ports.rest.JerseyAdapter;

import javax.ws.rs.client.ClientBuilder;
import java.time.LocalDate;

public class Main {
    public static void main(String[] args) throws CommunicationException {
        final Port port = new JerseyAdapter(
            ClientBuilder.newClient()
                .target("http://localhost:8080")
        );

        System.out.println(port.send(new AggiornaPersona("aa00", new Persona("asd", "dsa", LocalDate.now()))));;
        System.out.println(port.send(new RicercaPersona("aaa-bbb-cc01", "nome:Manuel AND cognome:Rasc*")));;
    }
}
