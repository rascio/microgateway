package it.r.ports.api;

public interface Gateway {

    <T> T send(Message<T> message);
}
