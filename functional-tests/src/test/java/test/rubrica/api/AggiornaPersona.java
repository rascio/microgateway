package test.rubrica.api;

import it.r.ports.api.Command;
import lombok.Value;
import test.rubrica.api.Persona;

@Value
public class AggiornaPersona implements Command<String, Persona, Persona> {
    String id;
    Persona body;
}
