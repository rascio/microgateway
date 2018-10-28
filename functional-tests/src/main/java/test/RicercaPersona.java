package test;

import it.r.ports.api.Query;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import test.RicercaPersona.PersonaParameters;

import java.beans.ConstructorProperties;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RicercaPersona implements Query<String, PersonaParameters, String> {
    private String id;
    private PersonaParameters parameters;

    @Override
    public Class<String> responseType() {
        return String.class;
    }

    @Value
    public static class PersonaParameters {
        private String q;
    }
}
