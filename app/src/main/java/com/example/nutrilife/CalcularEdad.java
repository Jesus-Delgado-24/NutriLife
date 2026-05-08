package com.example.nutrilife;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class CalcularEdad {
    public static int calculateAge(String birthDateString) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate birthDate = LocalDate.parse(birthDateString, formatter);
            LocalDate currentDate = LocalDate.now();

            if ((birthDate != null) && (currentDate != null)) {
                return Period.between(birthDate, currentDate).getYears();
            } else {
                return 0;
            }
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
