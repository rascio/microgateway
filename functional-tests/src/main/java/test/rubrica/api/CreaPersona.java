package test.rubrica.api;

import it.r.ports.api.Command;
import it.r.ports.api.None;
import lombok.Value;

@Value
public class CreaPersona implements Command<None, Persona, String> {
    None id;
    Persona body;

}
