package test.rubrica.api;

import it.r.ports.rest.api.RestApiRegistry;

public class RubricaRestApi {
    public static RestApiRegistry REGISTRY = RestApiRegistry.builder()
        .get(RicercaPersona.class, "/persona")
        .post(CreaPersona.class, "/persona")
        .get(DettaglioPersona.class, "/persona/{id}")
        .post(AggiornaPersona.class, "/persona/{id}")
        .build();
}
