package it.r.microgateway.server.url;

import it.r.ports.api.Message;
import org.springframework.web.util.UriTemplate;

import java.util.List;
import java.util.Map;

abstract class SpringMessageMapping<R extends Message<?>> implements Comparable<SpringMessageMapping<? extends Message<?>>>{

    public final Class<R> type;
    private final String domain;
    private final UriTemplate template;

    protected SpringMessageMapping(String domain, String template, Class<R> type) {
        this.domain = domain;
        this.template = new UriTemplate(template);
        this.type = type;
    }

    @Override
    public int compareTo(SpringMessageMapping<? extends Message<?>> o) {
        int res = Integer.compare(template.getVariableNames().size(), o.template.getVariableNames().size());
        if (res == 0) {
            res = Integer.compare(o.template.toString().split("/").length, template.toString().split("/").length);
        }
        return res;
    }

    public final boolean matches(String uri) {
        return this.template.matches(uri);
    }

    protected final Map<String, String> extractPathParams(String uri) {
        return this.template.match(uri);
    }

    protected final List<String> pathParamsNames() {
        return this.template.getVariableNames();
    }

    protected final String uri(Map<?, ?> params) {
        final String baseUri = domain + template.expand((Map)params).toString();
        return baseUri;
    }

    abstract String link(Object api);

    abstract R createMessage(javax.servlet.http.HttpServletRequest request);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SpringMessageMapping api = (SpringMessageMapping) o;

        if (!domain.equals(api.domain)) return false;
        if (!template.equals(api.template)) return false;
        if (!type.equals(api.type)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + domain.hashCode();
        result = 31 * result + template.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Api{" +
            "type=" + type +
            ", domain='" + domain + '\'' +
            ", template=" + template +
            '}';
    }
}
