package test.rubrica.api;

import it.r.ports.rest.api.RestApiRegistry;

public class RubricaRestApi {
    public static RestApiRegistry REGISTRY = RestApiRegistry.builder()
        .query(RicercaPersona.class, "/persona")
        .command(CreaPersona.class, "/persona")
        .query(DettaglioPersona.class, "/persona/{id}")
        .command(AggiornaPersona.class, "/persona/{id}")
        .build();
}
