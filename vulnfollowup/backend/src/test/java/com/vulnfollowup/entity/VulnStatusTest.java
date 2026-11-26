package com.vulnfollowup.entity;

import org.junit.jupiter.api.Test;

import static com.vulnfollowup.entity.VulnStatus.CLOSED;
import static com.vulnfollowup.entity.VulnStatus.OPEN;
import static com.vulnfollowup.entity.VulnStatus.PENDING_REMEDIATION;
import static com.vulnfollowup.entity.VulnStatus.RESOLVED;
import static org.assertj.core.api.Assertions.assertThat;

// Ciclo de remediación: ABIERTA -> PENDIENTE -> RESUELTA -> CERRADA (+ reabrir)
class VulnStatusTest {

    @Test
    void cicloNormal() {
        assertThat(OPEN.canMoveTo(PENDING_REMEDIATION)).isTrue();
        assertThat(PENDING_REMEDIATION.canMoveTo(RESOLVED)).isTrue();
        assertThat(RESOLVED.canMoveTo(CLOSED)).isTrue();
    }

    @Test
    void reabrirUnaResuelta() {
        assertThat(RESOLVED.canMoveTo(PENDING_REMEDIATION)).isTrue();
    }

    @Test
    void noSePuedenSaltarPasos() {
        assertThat(OPEN.canMoveTo(RESOLVED)).isFalse();
        assertThat(OPEN.canMoveTo(CLOSED)).isFalse();
        assertThat(PENDING_REMEDIATION.canMoveTo(OPEN)).isFalse();
    }

    @Test
    void mismoEstadoPermitidoSalvoCerrada() {
        // Útil para solo cambiar el responsable o añadir una evidencia
        assertThat(OPEN.canMoveTo(OPEN)).isTrue();
        assertThat(PENDING_REMEDIATION.canMoveTo(PENDING_REMEDIATION)).isTrue();
        // CERRADA es definitiva
        assertThat(CLOSED.canMoveTo(CLOSED)).isFalse();
        assertThat(CLOSED.canMoveTo(PENDING_REMEDIATION)).isFalse();
    }
}
