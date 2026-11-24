package com.vulnfollowup.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

// Registro de auditoría: cada cambio relevante deja una fila (quién, qué, cuándo).
// Es la evidencia que un cliente financiero necesita enseñar para DORA, y en la
// Fase 3 es también un recurso compartido para la demo de concurrencia.
@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_type", nullable = false, length = 30)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(nullable = false, length = 30)
    private String action;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected AuditLog() {
    }

    public AuditLog(String entityType, Long entityId, String action, String username, String details) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.action = action;
        this.username = username;
        this.details = details;
    }

    public Long getId() {
        return id;
    }

    public String getEntityType() {
        return entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public String getAction() {
        return action;
    }

    public String getUsername() {
        return username;
    }

    public String getDetails() {
        return details;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
