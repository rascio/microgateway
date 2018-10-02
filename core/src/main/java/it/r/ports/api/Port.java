package it.r.ports.api;

public interface Port {

    <T> T send(Message<T> message) throws CommunicationException;
}
