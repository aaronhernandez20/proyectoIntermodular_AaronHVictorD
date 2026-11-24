package com.vulnfollowup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Punto de entrada del backend de VulnFollowUp.
// Al arrancar: Flyway crea/comprueba la BD (V1, V2...) y InitialDataLoader carga
// los ficheros iniciales (CU1 - Preparar el entorno).
@SpringBootApplication
public class VulnFollowUpApplication {

    public static void main(String[] args) {
        SpringApplication.run(VulnFollowUpApplication.class, args);
    }
}
