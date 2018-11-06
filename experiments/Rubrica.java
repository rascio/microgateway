package it.r.ports.api.test;

import it.r.ports.api.*;
import it.r.ports.api.test.Endpoint.Root;
import it.r.ports.api.test.Rubrica.Persona;
import lombok.Value;


interface Endpoint<P extends Endpoint<?>> {
    P getParent();


    interface Root extends Endpoint<Root> {

    }
    interface Contextualized<E extends Endpoint<?>, ID> extends Endpoint<E> {
        ID getId();
    }
    static void main(String[] args) {
        final Gateway gateway = null;
        final Rubrica rubrica = null;

        rubrica.aziende()
            .find("ciccio srl")
            .persone()
            .crea()
            .execute(null, new Persona());

        rubrica.aziende()
            .find("ciccio srl")
            .persone()
            .find("pippo")
            .elimina()
            .execute(null, null);

        rubrica.personaRandom()
            .execute(null, null)
            .getPersona()
            .getId()
            .getParent()
            .find("paperino")
            .dettaglio()
            .execute(null, null);


    }
}
interface ServiceCall<P, B, R> {
    R execute(P parameter, B body);

    static <
        T extends Request<? extends Endpoint<?>, P, B, R>,
        P, B, R
    >
    ServiceCall<P, B, R> of(Class<T> type) {
        return null;
    }
}
public interface Rubrica extends Endpoint<Root> {

    ServiceCall<None, None, PersonaDashboard> personaRandom();

    AziendeRepository aziende();

    interface AziendeRepository extends Endpoint<Rubrica> {
        AziendaApi find(String azienda);
    }

    interface AziendaApi extends Endpoint {
        PersoneRepository persone();
    }

    interface PersoneRepository extends Endpoint<Rubrica> {
        PersonaApi find(String id);
        default ServiceCall<None, Persona, DettaglioPersona> crea() {
            return ServiceCall.of(CreaPersona.class);
        }
    }

    interface PersonaApi extends Contextualized<PersoneRepository, String> {

        default ServiceCall<None, None, Persona> dettaglio() {
            return ServiceCall.of(DettaglioPersona.class);
        }

        default ServiceCall<None, None, None> elimina() {
            return ServiceCall.of(EliminaPersona.class);
        }
    }

    @Value class PersonaDashboard {
        Integer statistiche;
        DettaglioPersona persona;
    }

    interface DettaglioPersona extends Query<PersonaApi, None, Persona> {
    }

    interface CreaPersona extends Command<PersoneRepository, None, Persona, DettaglioPersona> {
        Persona getBody();
    }

    interface EliminaPersona extends Request<PersonaApi, None, None, None> {
    }

    class Persona {
    }
}
