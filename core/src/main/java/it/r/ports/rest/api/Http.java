package it.r.ports.rest.api;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Value
@AllArgsConstructor
public class Http {
    private HttpMethod method;
    private String path;
    private boolean multipart;

    public Http(HttpMethod method, String path) {
        this(method, path, false);
    }
}
