package com.vulnfollowup.dto;

import com.vulnfollowup.entity.AssetEnums.AssetStatus;
import com.vulnfollowup.entity.AssetEnums.AssetType;
import com.vulnfollowup.entity.AssetEnums.Environment;
import com.vulnfollowup.entity.AssetEnums.Exposure;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

// DTOs de activos. Nunca se expone la entidad JPA directamente: así controlamos
// qué entra (validación) y qué sale (sin relaciones LAZY ni datos internos).
public final class AssetDTOs {

    private AssetDTOs() {
    }

    // POST /api/assets (y también cada elemento de data/assets.json en CU1)
    public record AssetRequest(
            @NotBlank @Size(max = 120) String name,
            @NotNull AssetType type,
            @NotBlank @Size(max = 100) String owner,
            @NotNull Environment environment,
            Exposure exposure,             // opcional: INTERNAL por defecto
            @Size(max = 120) String providerName) {  // opcional: se vincula si existe
    }

    // PUT /api/assets/{id}: lleva la versión leída en el GET (bloqueo optimista)
    public record AssetUpdateRequest(
            @NotBlank @Size(max = 120) String name,
            @NotNull AssetType type,
            @NotBlank @Size(max = 100) String owner,
            @NotNull Environment environment,
            @NotNull Exposure exposure,
            @NotNull AssetStatus status,
            @NotNull Long version) {
    }

    public record AssetResponse(
            Long id,
            String name,
            AssetType type,
            String owner,
            Environment environment,
            AssetStatus status,
            Exposure exposure,
            Long providerId,
            String providerName,
            Long version,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
    }
}
