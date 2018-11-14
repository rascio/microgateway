package test.rubrica.server;

import it.r.ports.api.DefaultGateway.Module;
import it.r.ports.api.DefaultGateway.Registry;
import it.r.ports.api.Envelope;
import it.r.ports.api.Gateway;
import test.rubrica.api.*;
import test.rubrica.api.RicercaPersona.PersonaParameters;
import test.rubrica.api.RicercaPersona.PersonaResult;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class RubricaModule implements Module {

    private final Map<String, Persona> persons = new ConcurrentHashMap<>();

    @Override
    public void register(Registry registry, Gateway gateway) {
        registry
            .register(RicercaPersona.class, this::find)
            .register(AggiornaPersona.class, this::update)
            .register(DettaglioPersona.class, this::get)
            .register(CreaPersona.class, this::create);
    }


    private List<PersonaResult> find(Envelope<RicercaPersona> envelope) {
        final RicercaPersona query = envelope.getRequest();
        return persons.entrySet()
            .stream()
            .filter(e -> {
                final Persona p = e.getValue();
                return p.getNome().contains(query.getParameters().getQ())
                        || p.getCognome().contains(query.getParameters().getQ())
                        || p.getEta().toString().contains(query.getParameters().getQ());
                }
            )
            .map(e -> {
                final Persona p = e.getValue();

                final PersonaResult result = new PersonaResult(p.getNome(), p.getCognome());
                result.link("dettaglio",
                    new DettaglioPersona(e.getKey())
                );
                result.link("ricerca",
                    new RicercaPersona(
                        new PersonaParameters(p.getCognome())
                    )
                );
                return result;
            })
            .collect(Collectors.toList());
    }

    private String create(Envelope<CreaPersona> envelope) {
        final CreaPersona crea = envelope.getRequest();

        final String id = UUID.randomUUID().toString();
        persons.put(id, crea.getBody());
        return id;
    }

    private Persona update(Envelope<AggiornaPersona> envelope) {
        final AggiornaPersona cmd = envelope.getRequest();
        return persons.put(cmd.getId(), cmd.getBody());
    }

    private Persona get(Envelope<DettaglioPersona> envelope) {
        final DettaglioPersona query = envelope.getRequest();
        return persons.get(query.getId());
    }
}
