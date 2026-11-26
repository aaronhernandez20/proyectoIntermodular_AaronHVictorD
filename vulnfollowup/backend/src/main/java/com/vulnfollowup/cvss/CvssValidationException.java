package com.vulnfollowup.cvss;

import java.util.List;

// Valoración CVSS incoherente -> 400 con la lista de errores concretos
// (GlobalExceptionHandler la convierte en { "message": ..., "errors": [...] }).
public class CvssValidationException extends IllegalArgumentException {

    private final transient List<String> errors;

    public CvssValidationException(List<String> errors) {
        super("Valoración CVSS incoherente: " + String.join("; ", errors));
        this.errors = List.copyOf(errors);
    }

    public List<String> getErrors() {
        return errors;
    }
}
