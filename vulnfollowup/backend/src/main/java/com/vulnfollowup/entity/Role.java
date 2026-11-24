package com.vulnfollowup.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Rol de la aplicación: ADMIN, ANALYST o VIEWER (tabla app_role).
// El nombre se guarda sin el prefijo ROLE_; Spring lo añade al construir las authorities.
@Entity
@Table(name = "app_role")
public class Role {

    public static final String ADMIN = "ADMIN";
    public static final String ANALYST = "ANALYST";
    public static final String VIEWER = "VIEWER";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String name;

    protected Role() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
