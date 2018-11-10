package it.r.ports.utils;

import it.r.ports.api.Envelope;
import it.r.ports.api.Gateway;
import it.r.ports.api.Request;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AllArgsConstructor
public class AuditGateway implements Gateway {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuditGateway.class);

    private final Gateway gateway;

    @Override
    public <R> R send(Envelope<? extends Request<?, ?, ?, R>> message) {
        try {
            final R result = gateway.send(message);
            LOGGER.debug("Exchanging:\n\t--> {}\n\t<-- {}", message, result);
            return result;
        }
        catch (Throwable t) {
            LOGGER.debug("Error:\n\t--> {}\n\txxx {}", message, t.getMessage());
            throw t;
        }

    }
}
