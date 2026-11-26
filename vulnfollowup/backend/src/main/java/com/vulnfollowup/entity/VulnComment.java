package com.vulnfollowup.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

// Comentario de remediación con evidencia opcional (CU4): "Parche aplicado", "Ticket CR-1042"...
@Entity
@Table(name = "vuln_comment")
public class VulnComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vulnerability_id", nullable = false)
    private Vulnerability vulnerability;

    @Column(nullable = false, length = 50)
    private String author;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(columnDefinition = "TEXT")
    private String evidence;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected VulnComment() {
    }

    public VulnComment(String author, String text, String evidence) {
        this.author = author;
        this.text = text;
        this.evidence = evidence;
    }

    void setVulnerability(Vulnerability vulnerability) {
        this.vulnerability = vulnerability;
    }

    public Long getId() { return id; }
    public String getAuthor() { return author; }
    public String getText() { return text; }
    public String getEvidence() { return evidence; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
