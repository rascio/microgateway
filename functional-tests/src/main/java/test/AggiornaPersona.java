package test;

import it.r.ports.api.Command;
import it.r.ports.api.Message;
import it.r.ports.api.Query;
import it.r.ports.rest.api.Http;
import it.r.ports.rest.api.HttpMethod;
import lombok.Value;

@Value
public class AggiornaPersona implements Command<String, Persona, Void> {
    String id;
    Persona body;

    @Override
    public Class<Void> responseType() {
        return Void.class;
    }
}
