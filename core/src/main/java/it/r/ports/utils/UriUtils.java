package it.r.ports.utils;

import com.google.common.collect.ImmutableMap;
import it.r.ports.api.Request;
import it.r.ports.rest.api.Http;
import org.apache.commons.beanutils.BeanMap;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

public class UriUtils {

    public static URI toURI(Request<?, ?, ?, ?> message, Http http, UriBuilder builder) {
        return builder.path(http.getPath())
            .queryParams(toMultiValueMap(message.getParameters()))
            .build(idToMap(message));
    }

    private static MultiValueMap<String, String> toMultiValueMap(Object parameters) {
        if (parameters == null) {
            return new LinkedMultiValueMap<>();
        }
        else if (parameters instanceof MultiValueMap) {
            return (MultiValueMap<String, String>) parameters;
        }
        else if (parameters instanceof Map) {
            final MultiValueMap<String, String> result = new LinkedMultiValueMap<>();
            ((Map) parameters).forEach((k, v) -> result.put(k.toString(), listForParameter(v)));
            return result;
        }
        else {
            return toMultiValueMap(beanMap(parameters));

        }
    }

    private static List<String> listForParameter(Object v) {
        final List<String> values;
        if (v instanceof List) {
            values = ((List<?>) v)
                .stream()
                .map(UriUtils::toStringEncoded)
                .collect(Collectors.toList());
        } else {
            values = Collections.singletonList(toStringEncoded(v));
        }
        return values;
    }

    private static String toStringEncoded(Object v) {
        try {
            return URLEncoder.encode(Objects.toString(v), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<String, Object> idToMap(Request req) {
        final Object id = req.getId();
        if (id == null){
            return Collections.emptyMap();
        }
        else if (id instanceof Map) {
            return (Map<String, Object>) id;
        }
        else {
            return ImmutableMap.of("id", id);
        }
    }

    private static Map<String, Object> beanMap(Object parameters) {
        final BeanMap map = new BeanMap(parameters);
        final Map<String, Object> res = new HashMap(map);
        res.remove("class");

        return res;
    }
}
