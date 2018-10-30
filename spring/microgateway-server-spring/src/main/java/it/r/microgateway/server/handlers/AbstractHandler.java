package it.r.microgateway.server.handlers;

import it.r.ports.api.Gateway;
import it.r.ports.api.None;
import it.r.ports.api.Request;
import it.r.ports.utils.BeanUtils;
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
import java.util.Optional;

@AllArgsConstructor
abstract class AbstractHandler implements HandlerFunction<ServerResponse> {

    protected final Class<? extends Request<?, ?, ?, ?>> type;
    protected final Gateway gateway;
    protected final ConversionService conversionService;

    protected Optional<Object> convertParameters(ServerRequest request) {
        final Map<String, Object> fields = new HashMap<>();

        request.queryParams().forEach((k, l) -> {
            if (l.size() > 1) {
                fields.put(k, l);
            } else {
                fields.put(k, l.get(0));
            }
        });
        return typeOfField(type, "parameters")
            .map(type -> BeanUtils.newInstance(type, fields));
    }

    protected Optional<Class<?>> typeOfField(Class<? extends Request> type, String prop) {
        return Introspection.read(type, prop)
            .map(PropertyDescriptor::getPropertyType);
            //.orElseThrow(() -> new RuntimeException("Missing " + type.getName() + "." + prop));
    }

    protected Optional<Object> convertId(ServerRequest request) {
        return typeOfField(type, "id")
            .filter(type -> !None.class.isAssignableFrom(type))
            .map(type -> {
//                if (type == None.class) {
//                    return None.INSTANCE;
//                }
                final Map<String, String> params = request.pathVariables();
                if (params.size() == 1) {
                    return conversionService.convert(params.values().iterator().next(), type);
                }
                return conversionService.convert(params, type);
            });
    }
}
