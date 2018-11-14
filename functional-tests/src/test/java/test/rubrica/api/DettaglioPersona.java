package test.rubrica.api;

import it.r.ports.api.None;
import it.r.ports.api.Query;
import lombok.Value;

@Value
public class DettaglioPersona implements Query<String, None, Persona>{
    String id;
    None parameters = None.INSTANCE;
}
