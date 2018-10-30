package test.rubrica.api;

import it.r.ports.api.StaticQuery;
import it.r.ports.api.None;
import it.r.ports.api.Query;
import lombok.Value;
import test.rubrica.api.RicercaPersona.PersonaParameters;

import java.util.List;

@Value
public class RicercaPersona implements StaticQuery<PersonaParameters, List<Persona>> {
    PersonaParameters parameters;

    @Value
    public static class PersonaParameters {
        private String q;
    }
}
