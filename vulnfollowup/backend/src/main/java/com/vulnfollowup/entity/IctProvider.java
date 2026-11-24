package com.vulnfollowup.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Proveedor TIC (SGE / DORA). En la Fase 1 solo se mapea la entidad porque el activo
// la referencia; los endpoints, la regla "en riesgo" y el informe van en la Fase 3.
@Entity
@Table(name = "ict_provider")
public class IctProvider {

    public enum Criticality { LOW, MEDIUM, HIGH, CRITICAL }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String name;

    @Column(name = "service_provided", nullable = false, length = 200)
    private String serviceProvided;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Criticality criticality;

    @Column(name = "contract_ref", length = 60)
    private String contractRef;

    @Column(name = "contract_end")
    private LocalDate contractEnd;

    @Column(name = "next_review_date")
    private LocalDate nextReviewDate;

    @Column(name = "contact_name", length = 100)
    private String contactName;

    @Column(name = "contact_email", length = 120)
    private String contactEmail;

    @Column(nullable = false)
    private boolean active = true;

    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    protected IctProvider() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getServiceProvided() {
        return serviceProvided;
    }

    public Criticality getCriticality() {
        return criticality;
    }

    public String getContractRef() {
        return contractRef;
    }

    public LocalDate getContractEnd() {
        return contractEnd;
    }

    public LocalDate getNextReviewDate() {
        return nextReviewDate;
    }

    public String getContactName() {
        return contactName;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public boolean isActive() {
        return active;
    }

    public Long getVersion() {
        return version;
    }
}
