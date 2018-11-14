package test.rubrica.api;

import it.r.ports.api.StaticCommand;
import lombok.Value;

@Value
public class CreaPersona implements StaticCommand<Persona, String> {
    final Persona body;

}
