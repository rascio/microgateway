package test.rubrica.api;

import it.r.ports.api.StaticQuery;
import it.r.ports.hypermedia.api.Resource;
import lombok.ToString;
import lombok.Value;
import test.rubrica.api.RicercaPersona.PersonaParameters;
import test.rubrica.api.RicercaPersona.PersonaResult;

import java.util.List;

@Value
public class RicercaPersona implements StaticQuery<PersonaParameters, List<PersonaResult>> {
    PersonaParameters parameters;

    @Value
    public static class PersonaParameters {
        private String q;
    }

    @Value
    public static class PersonaResult extends Resource {
        private String nome;
        private String cognome;

        @Override
        public String toString() {
            return "PersonaResult[" +
                "nome='" + nome + '\'' +
                ", cognome='" + cognome + '\'' +
                ", links='" + getLinks() +
                ']';
        }
    }
}
