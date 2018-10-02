package test;

import lombok.Value;

import java.time.LocalDate;

@Value
public class Persona {
    String nome;
    String cognome;
    LocalDate dataNascita;
}
