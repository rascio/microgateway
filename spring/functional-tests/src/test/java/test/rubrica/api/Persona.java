package test.rubrica.api;

import lombok.Value;

import java.time.LocalDate;
import java.util.Date;

@Value
public class Persona {
    private String nome;
    private String cognome;
    private Integer eta;
}
