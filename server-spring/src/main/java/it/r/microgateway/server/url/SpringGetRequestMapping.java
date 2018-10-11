package it.r.microgateway.server.url;

import com.google.common.collect.ImmutableSet;
import it.r.ports.api.Message;
import it.r.ports.utils.BeanUtils;
import org.apache.commons.beanutils.BeanMap;
import org.springframework.web.util.UriUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Set;

class SpringGetRequestMapping<T extends Message<?>> extends SpringMessageMapping<T>{

    SpringGetRequestMapping(String domain, String template, Class<T> type) {
        super(domain, template, type);
    }

    @Override
    public String link(Object api) {
        final Map<?, ?> map = new BeanMap(api);
        final String baseUri = uri(map);
        return baseUri + queryParams(map);
    }

    @Override
    public T createMessage(HttpServletRequest request) {
        final Map<String, String[]> params = request.getParameterMap();
        final Map<String, String> pathParams = extractPathParams(request.getRequestURI());
        final T instance = BeanUtils.newInstance(this.type);
        return BeanUtils.populate(BeanUtils.populate(instance, pathParams), params);
    }

    private String queryParams(Map<?, ?> beanMap) {
        final Set<String> ignore = ImmutableSet.<String>builder()
            .add("id")
            .add("class")
            .addAll(pathParamsNames())
            .build();

        final StringBuilder uri = new StringBuilder("?");

        beanMap.forEach((name, value) -> {
            if (value != null && !ignore.contains(name)) {
                try {
                    uri.append(name)
                        .append("=")
                        .append(UriUtils.encodeQueryParam(value.toString(), "UTF-8"))
                        .append("&");
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        return uri.substring(0, uri.length() - 1).toString();
    }
}
