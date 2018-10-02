package test;

import it.r.ports.api.Message;
import it.r.ports.rest.api.Http;
import it.r.ports.rest.api.HttpMethod;
import lombok.Value;

@Value
@Http(method = HttpMethod.POST, path = "/persona/{id}")
public class AggiornaPersona implements Message<Void>{
    String id;
    Persona persona;

    @Override
    public Class<Void> responseType() {
        return Void.class;
    }
}
