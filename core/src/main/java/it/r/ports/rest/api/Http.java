package it.r.ports.rest.api;

import lombok.AllArgsConstructor;
import lombok.Value;

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
