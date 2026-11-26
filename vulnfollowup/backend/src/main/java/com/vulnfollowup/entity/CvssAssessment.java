package com.vulnfollowup.entity;

import com.vulnfollowup.cvss.Severity;
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
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Histórico de valoraciones CVSS de una vulnerabilidad. Nunca se modifica ni se borra:
// si cambia la valoración se añade una fila nueva (trazabilidad para DORA).
@Entity
@Table(name = "cvss_assessment")
public class CvssAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vulnerability_id", nullable = false)
    private Vulnerability vulnerability;

    @Column(name = "cvss_version", nullable = false, length = 3)
    private String cvssVersion;

    @Column(name = "cvss_vector", nullable = false, length = 200)
    private String cvssVector;

    @Column(name = "cvss_score", nullable = false, precision = 3, scale = 1)
    private BigDecimal cvssScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Severity severity;

    @Column(name = "assessed_by", nullable = false, length = 50)
    private String assessedBy;

    @Column(name = "assessed_at", nullable = false, updatable = false)
    private LocalDateTime assessedAt = LocalDateTime.now();

    protected CvssAssessment() {
    }

    // Copia la valoración vigente de la vulnerabilidad en ese momento
    public CvssAssessment(Vulnerability v, String assessedBy) {
        this.vulnerability = v;
        this.cvssVersion = v.getCvssVersion();
        this.cvssVector = v.getCvssVector();
        this.cvssScore = v.getCvssScore();
        this.severity = v.getSeverity();
        this.assessedBy = assessedBy;
    }

    public Long getId() { return id; }
    public String getCvssVersion() { return cvssVersion; }
    public String getCvssVector() { return cvssVector; }
    public BigDecimal getCvssScore() { return cvssScore; }
    public Severity getSeverity() { return severity; }
    public String getAssessedBy() { return assessedBy; }
    public LocalDateTime getAssessedAt() { return assessedAt; }
}
