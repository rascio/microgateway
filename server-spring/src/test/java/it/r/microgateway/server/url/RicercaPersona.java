package it.r.microgateway.server.url;

import it.r.ports.api.Message;
import it.r.ports.rest.api.Http;
import it.r.ports.rest.api.HttpMethod;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
@Http(method = HttpMethod.GET, path = "/{idAzienda}/ricerca")
public class RicercaPersona implements Message<String> {
    private String idAzienda;
    private String q;

    @Override
    public Class<String> responseType() {
        return String.class;
    }
}
