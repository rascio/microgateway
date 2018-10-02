package test;

import it.r.ports.api.Message;
import it.r.ports.rest.api.Http;
import it.r.ports.rest.api.HttpMethod;
import lombok.Value;

@Value
@Http(method = HttpMethod.GET, path = "/{idAzienda}/ricerca")
public class RicercaPersona implements Message<String> {
    private String idAzienda;
    private String q;

    @Override
    public Class<String> responseType() {
        return String.class;
    }
}
