package com.vulnfollowup.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.List;

// DTOs del login agrupados (son pequeños y solo los usa AuthController)
public final class AuthDTOs {

    private AuthDTOs() {
    }

    public record LoginRequest(
            @NotBlank String username,
            @NotBlank String password) {
    }

    // El cliente (web o Android) guarda el token y lo envía en cada petición:
    // Authorization: Bearer <accessToken>
    public record LoginResponse(
            String accessToken,
            String tokenType,
            Instant expiresAt,
            String username,
            String fullName,
            List<String> roles) {
    }

    public record MeResponse(String username, String fullName, List<String> roles) {
    }
}
