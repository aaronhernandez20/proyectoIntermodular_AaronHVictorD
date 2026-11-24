package com.vulnfollowup.entity;

// Enumerados del activo agrupados en un solo fichero para no llenar el paquete
// de clases de 3 líneas. Los valores coinciden con los CHECK de V1__schema.sql.
public final class AssetEnums {

    private AssetEnums() {
    }

    public enum AssetType { API, SERVER, DATABASE, WEB_APP, MOBILE_APP, NETWORK, OTHER }

    public enum Environment { PRODUCTION, STAGING, TEST, DEVELOPMENT }

    // Desactivar un activo no lo borra: pasa a INACTIVE y se conserva su historial.
    public enum AssetStatus { ACTIVE, INACTIVE }

    // Exposición: un activo expuesto a Internet tiene más prioridad operativa
    // aunque su CVSS sea el mismo (apartado 10 del enunciado).
    public enum Exposure { INTERNET, INTERNAL }
}
