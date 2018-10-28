package it.r.ports.api;

public interface Gateway {

//    <T> T send(Message<T> message);

    <I, P, B, T> T send(Request<I, P, B, T> message);
}
