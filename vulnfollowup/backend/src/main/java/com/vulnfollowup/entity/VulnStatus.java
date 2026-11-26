package com.vulnfollowup.entity;

import java.util.Set;

// Ciclo de remediación (apartado 4 del enunciado):
//   OPEN -> PENDING_REMEDIATION -> RESOLVED -> CLOSED
// y RESOLVED -> PENDING_REMEDIATION para reabrir si la corrección no funcionó.
// Las transiciones viven en el propio enum: una sola fuente de verdad, fácil de testear.
public enum VulnStatus {
    OPEN,
    PENDING_REMEDIATION,
    RESOLVED,
    CLOSED;

    public Set<VulnStatus> allowedNext() {
        return switch (this) {
            case OPEN -> Set.of(PENDING_REMEDIATION);
            case PENDING_REMEDIATION -> Set.of(RESOLVED);
            case RESOLVED -> Set.of(CLOSED, PENDING_REMEDIATION);
            case CLOSED -> Set.of();      // CERRADA es definitiva
        };
    }

    // Quedarse en el mismo estado se permite (p. ej. solo cambiar responsable o añadir evidencia),
    // salvo en CLOSED: una vulnerabilidad cerrada ya no se modifica (es la evidencia final).
    public boolean canMoveTo(VulnStatus target) {
        if (this == CLOSED) return false;
        return this == target || allowedNext().contains(target);
    }
}
