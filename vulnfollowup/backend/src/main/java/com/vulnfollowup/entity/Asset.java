package com.vulnfollowup.entity;

import com.vulnfollowup.entity.AssetEnums.AssetStatus;
import com.vulnfollowup.entity.AssetEnums.AssetType;
import com.vulnfollowup.entity.AssetEnums.Environment;
import com.vulnfollowup.entity.AssetEnums.Exposure;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.LocalDateTime;

// Activo de la empresa cliente: una API de pagos, un servidor, una base de datos...
// Campos obligatorios del enunciado: nombre, tipo, responsable, entorno y estado.
@Entity
@Table(name = "asset")
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssetType type;

    // Responsable del activo (persona o equipo)
    @Column(nullable = false, length = 100)
    private String owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Environment environment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssetStatus status = AssetStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Exposure exposure = Exposure.INTERNAL;

    // Proveedor TIC que presta el activo (puede no tener). LAZY: solo se carga si se usa.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id")
    private IctProvider provider;

    // Bloqueo optimista: Hibernate añade "WHERE version = ?" en cada UPDATE.
    // Si otro analista lo ha cambiado antes, se actualizan 0 filas -> 409 Conflict.
    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    protected Asset() {
    }

    public Asset(String name, AssetType type, String owner, Environment environment, Exposure exposure) {
        this.name = name;
        this.type = type;
        this.owner = owner;
        this.environment = environment;
        this.exposure = exposure != null ? exposure : Exposure.INTERNAL;
    }

    // Toda la modificación pasa por aquí: así la entidad controla sus propios cambios
    public void update(String name, AssetType type, String owner, Environment environment,
                       Exposure exposure, AssetStatus status) {
        this.name = name;
        this.type = type;
        this.owner = owner;
        this.environment = environment;
        this.exposure = exposure;
        this.status = status;
    }

    public void deactivate() {
        this.status = AssetStatus.INACTIVE;
    }

    public void setProvider(IctProvider provider) {
        this.provider = provider;
    }

    @PreUpdate
    void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public AssetType getType() {
        return type;
    }

    public String getOwner() {
        return owner;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public AssetStatus getStatus() {
        return status;
    }

    public Exposure getExposure() {
        return exposure;
    }

    public IctProvider getProvider() {
        return provider;
    }

    public Long getVersion() {
        return version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
