package test.rubrica;

import it.r.ports.api.DefaultGateway.Module;
import it.r.ports.api.DefaultGateway.Registry;
import test.rubrica.api.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class RubricaModule implements Module {

    private final Map<String, Persona> persons = new ConcurrentHashMap<>();

    @Override
    public void register(Registry registry) {
        registry
            .register(RicercaPersona.class, this::find)
            .register(AggiornaPersona.class, this::update)
            .register(DettaglioPersona.class, this::get)
            .register(CreaPersona.class, this::create);
    }


    private List<Persona> find(RicercaPersona query) {
        return persons.values()
            .stream()
            .filter(p -> p.getNome().contains(query.getParameters().getQ())
                || p.getCognome().contains(query.getParameters().getQ())
                || p.getEta().toString().contains(query.getParameters().getQ())
            )
            .collect(Collectors.toList());
    }

    private String create(CreaPersona crea) {
        final String id = UUID.randomUUID().toString();
        persons.put(id, crea.getBody());
        return id;
    }

    private Persona update(AggiornaPersona cmd) {
        return persons.put(cmd.getId(), cmd.getBody());
    }

    private Persona get(DettaglioPersona query) {
        return persons.get(query.getId());
    }
}
