package it.r.ports.api;

public class None {

    public static None INSTANCE = new None();

    private None() {

    }

    @Override
    public String toString() {
        return "[None]";
    }
}
