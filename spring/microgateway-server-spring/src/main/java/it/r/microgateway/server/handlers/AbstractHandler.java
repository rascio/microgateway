package it.r.microgateway.server.handlers;

import it.r.ports.api.Gateway;
import it.r.ports.api.None;
import it.r.ports.api.Request;
import it.r.ports.utils.Introspection;
import lombok.AllArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
abstract class AbstractHandler implements HandlerFunction<ServerResponse> {

    protected final Class<? extends Request> type;
    protected final Gateway gateway;
    protected final ConversionService conversionService;

    protected Map<String, Object> toSimpleMap(MultiValueMap<String, ?> parameters) {
        final Map<String, Object> m = new HashMap<>();
        parameters.forEach((k, l) -> {
            if (l.size() > 1) {
                m.put(k, l);
            } else {
                m.put(k, l.get(0));
            }
        });
        return m;
    }

    protected Class<?> typeOfField(Class<? extends Request> type, String body) {
        return Introspection.read(type, body)
            .map(PropertyDescriptor::getPropertyType)
            .orElseThrow(() -> new RuntimeException("Missing " + type.getName() + "." + body));
    }

    protected Object convertId(ServerRequest request, Class<?> type) {
        if (type == None.class) {
            return None.INSTANCE;
        }
        final Map<String, String> params = request.pathVariables();
        System.out.println(params);
        if (params.size() == 1) {
            return conversionService.convert(params.values().iterator().next(), type);
        }
        return conversionService.convert(params, type);
    }
}
