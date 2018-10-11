package it.r.microgateway.server.url;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import it.r.ports.api.Message;

public interface RequestUrlMapper {

    String resolve(Message<?> request);
    
    Message<?> resolve(HttpServletRequest request) throws IOException;

    <C extends Message<?>> void register(Class<C> type);

}
