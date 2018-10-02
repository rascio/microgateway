package it.r.ports.api;

public interface Message<R> {

    Class<R> responseType();
}
