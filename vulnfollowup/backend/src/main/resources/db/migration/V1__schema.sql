-- =====================================================================
-- VulnFollowUp - V1: esquema completo de la base de datos (PostgreSQL 16)
-- Flyway lo ejecuta solo una vez al arrancar el backend (CU1).
-- Las reglas importantes también están aquí (CHECK, UNIQUE, FK) para que
-- la integridad no dependa solo del código Java.
-- =====================================================================

-- ---------- Seguridad: usuarios y roles ----------
CREATE TABLE app_role (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(20) NOT NULL UNIQUE
         CHECK (name IN ('ADMIN', 'ANALYST', 'VIEWER'))
);

CREATE TABLE app_user (
    id         BIGSERIAL PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    full_name  VARCHAR(100) NOT NULL,
    password   VARCHAR(200) NOT NULL,          -- hash BCrypt, nunca texto plano
    enabled    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP    NOT NULL DEFAULT now()
);

-- N:M usuario <-> rol (igual que en la práctica de Spring Security)
CREATE TABLE app_user_role (
    user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES app_role(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- ---------- SGE: proveedores TIC (DORA) ----------
CREATE TABLE ict_provider (
    id               BIGSERIAL PRIMARY KEY,
    name             VARCHAR(120) NOT NULL UNIQUE,
    service_provided VARCHAR(200) NOT NULL,
    criticality      VARCHAR(10)  NOT NULL
                     CHECK (criticality IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    contract_ref     VARCHAR(60),
    contract_end     DATE,
    next_review_date DATE,
    contact_name     VARCHAR(100),
    contact_email    VARCHAR(120),
    active           BOOLEAN   NOT NULL DEFAULT TRUE,
    version          BIGINT    NOT NULL DEFAULT 0,
    created_at       TIMESTAMP NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP NOT NULL DEFAULT now()
);

-- ---------- Activos ----------
-- Un activo lo presta como mucho un proveedor TIC (N:1).
CREATE TABLE asset (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(120) NOT NULL UNIQUE,
    type        VARCHAR(20)  NOT NULL
                CHECK (type IN ('API', 'SERVER', 'DATABASE', 'WEB_APP', 'MOBILE_APP', 'NETWORK', 'OTHER')),
    owner       VARCHAR(100) NOT NULL,           -- responsable del activo
    environment VARCHAR(20)  NOT NULL
                CHECK (environment IN ('PRODUCTION', 'STAGING', 'TEST', 'DEVELOPMENT')),
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
                CHECK (status IN ('ACTIVE', 'INACTIVE')),
    exposure    VARCHAR(20)  NOT NULL DEFAULT 'INTERNAL'
                CHECK (exposure IN ('INTERNET', 'INTERNAL')),  -- para la prioridad operativa
    provider_id BIGINT REFERENCES ict_provider(id) ON DELETE SET NULL,
    version     BIGINT    NOT NULL DEFAULT 0,      -- bloqueo optimista (@Version)
    created_at  TIMESTAMP NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP NOT NULL DEFAULT now()
);

-- ---------- Vulnerabilidades ----------
-- Guardamos aquí la valoración CVSS vigente (para filtrar rápido por puntuación
-- y severidad) y el histórico completo en cvss_assessment.
CREATE TABLE vulnerability (
    id             BIGSERIAL PRIMARY KEY,
    code           VARCHAR(30)  NOT NULL UNIQUE,   -- CVE-2021-44228 o VFU-0001
    title          VARCHAR(200) NOT NULL,
    description    TEXT         NOT NULL,
    published_date DATE         NOT NULL,
    status         VARCHAR(30)  NOT NULL DEFAULT 'OPEN'
                   CHECK (status IN ('OPEN', 'PENDING_REMEDIATION', 'RESOLVED', 'CLOSED')),
    cvss_version   VARCHAR(3)   NOT NULL CHECK (cvss_version IN ('3.1', '4.0')),
    cvss_vector    VARCHAR(200) NOT NULL,
    cvss_score     NUMERIC(3,1) NOT NULL CHECK (cvss_score BETWEEN 0.0 AND 10.0),
    severity       VARCHAR(10)  NOT NULL
                   CHECK (severity IN ('NONE', 'LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    assignee_id    BIGINT REFERENCES app_user(id) ON DELETE SET NULL,  -- responsable de remediar
    target_date    DATE,                                                -- fecha objetivo
    reference_urls TEXT,                                                -- URLs separadas por salto de línea
    version        BIGINT    NOT NULL DEFAULT 0,
    created_at     TIMESTAMP NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP NOT NULL DEFAULT now(),
    -- Coherencia versión-vector: 3.1 => "CVSS:3.1/...", 4.0 => "CVSS:4.0/..."
    CONSTRAINT chk_vuln_vector_version CHECK (cvss_vector LIKE 'CVSS:' || cvss_version || '/%'),
    -- Coherencia puntuación-severidad (tabla 2 del enunciado)
    CONSTRAINT chk_vuln_severity_score CHECK (
        (severity = 'NONE'     AND cvss_score = 0.0) OR
        (severity = 'LOW'      AND cvss_score BETWEEN 0.1 AND 3.9) OR
        (severity = 'MEDIUM'   AND cvss_score BETWEEN 4.0 AND 6.9) OR
        (severity = 'HIGH'     AND cvss_score BETWEEN 7.0 AND 8.9) OR
        (severity = 'CRITICAL' AND cvss_score BETWEEN 9.0 AND 10.0))
);

-- N:M vulnerabilidad <-> activo
CREATE TABLE vulnerability_asset (
    vulnerability_id BIGINT NOT NULL REFERENCES vulnerability(id) ON DELETE CASCADE,
    asset_id         BIGINT NOT NULL REFERENCES asset(id)         ON DELETE CASCADE,
    PRIMARY KEY (vulnerability_id, asset_id)
);

-- Histórico de valoraciones CVSS (POST /api/vulnerabilities/{id}/assessment)
CREATE TABLE cvss_assessment (
    id               BIGSERIAL PRIMARY KEY,
    vulnerability_id BIGINT       NOT NULL REFERENCES vulnerability(id) ON DELETE CASCADE,
    cvss_version     VARCHAR(3)   NOT NULL CHECK (cvss_version IN ('3.1', '4.0')),
    cvss_vector      VARCHAR(200) NOT NULL,
    cvss_score       NUMERIC(3,1) NOT NULL CHECK (cvss_score BETWEEN 0.0 AND 10.0),
    severity         VARCHAR(10)  NOT NULL,
    assessed_by      VARCHAR(50)  NOT NULL,
    assessed_at      TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT chk_assess_vector_version CHECK (cvss_vector LIKE 'CVSS:' || cvss_version || '/%')
);

-- Comentarios y evidencias de remediación (CU4)
CREATE TABLE vuln_comment (
    id               BIGSERIAL PRIMARY KEY,
    vulnerability_id BIGINT      NOT NULL REFERENCES vulnerability(id) ON DELETE CASCADE,
    author           VARCHAR(50) NOT NULL,
    text             TEXT        NOT NULL,
    evidence         TEXT,
    created_at       TIMESTAMP   NOT NULL DEFAULT now()
);

-- Auditoría: quién cambió qué y cuándo (evidencias para DORA)
CREATE TABLE audit_log (
    id          BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(30) NOT NULL,
    entity_id   BIGINT      NOT NULL,
    action      VARCHAR(30) NOT NULL,
    username    VARCHAR(50) NOT NULL,
    details     TEXT,
    created_at  TIMESTAMP   NOT NULL DEFAULT now()
);

-- Índices para los filtros más usados
CREATE INDEX idx_vuln_status   ON vulnerability(status);
CREATE INDEX idx_vuln_severity ON vulnerability(severity);
CREATE INDEX idx_vuln_score    ON vulnerability(cvss_score);
CREATE INDEX idx_vuln_assignee ON vulnerability(assignee_id);
CREATE INDEX idx_asset_provider ON asset(provider_id);
CREATE INDEX idx_audit_entity  ON audit_log(entity_type, entity_id);
