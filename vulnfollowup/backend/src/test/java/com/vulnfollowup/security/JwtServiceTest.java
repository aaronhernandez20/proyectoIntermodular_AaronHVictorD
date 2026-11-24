package com.vulnfollowup.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Test unitario sin levantar Spring: comprueba que el token lleva el usuario y los roles,
// y que un token manipulado (o firmado con otra clave) se rechaza.
class JwtServiceTest {

    private static final String SECRET = "clave-de-test-con-mas-de-32-caracteres-123";

    @Test
    void tokenContieneUsuarioYRoles() {
        JwtService service = new JwtService(SECRET, 60);

        String token = service.issue("analyst", List.of("ANALYST")).value();
        Jwt jwt = service.decoder().decode(token);

        assertThat(jwt.getSubject()).isEqualTo("analyst");
        assertThat(jwt.getClaimAsStringList(JwtService.ROLES_CLAIM)).containsExactly("ANALYST");
        assertThat(jwt.getExpiresAt()).isAfter(jwt.getIssuedAt());
    }

    @Test
    void tokenFirmadoConOtraClaveSeRechaza() {
        JwtService atacante = new JwtService("otra-clave-distinta-de-mas-de-32-caracteres", 60);
        JwtService servidor = new JwtService(SECRET, 60);

        String tokenFalso = atacante.issue("viewer", List.of("ADMIN")).value();

        assertThatThrownBy(() -> servidor.decoder().decode(tokenFalso))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void claveDemasiadoCortaNoSeAcepta() {
        assertThatThrownBy(() -> new JwtService("corta", 60))
                .isInstanceOf(IllegalStateException.class);
    }
}
