package test.rubrica.api;

import it.r.ports.api.None;
import it.r.ports.api.Query;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import test.rubrica.api.RicercaPersona.PersonaParameters;

import java.util.List;

@Value
public class RicercaPersona implements Query<None, PersonaParameters, List<Persona>> {
    None id = None.INSTANCE;
    PersonaParameters parameters;

    @Value
    public static class PersonaParameters {
        private String q;
    }
}
