package test.rubrica.api;

import it.r.ports.api.Command;
import it.r.ports.api.None;
import it.r.ports.api.StaticCommand;
import lombok.Value;

@Value
public class CreaPersona implements Command<None, Persona, String> {
    final None id = None.INSTANCE;
    final Persona body;

}
