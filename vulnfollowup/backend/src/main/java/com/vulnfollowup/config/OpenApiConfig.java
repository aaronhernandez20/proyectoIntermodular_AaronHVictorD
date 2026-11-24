package com.vulnfollowup.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

// Configura Swagger UI (/swagger-ui.html) para que tenga el botón "Authorize":
// se hace login en POST /api/auth/login, se pega el accessToken y desde ese momento
// Swagger envía "Authorization: Bearer <token>" en todas las peticiones.
@Configuration
@OpenAPIDefinition(
        info = @Info(title = "VulnFollowUp API", version = "0.1.0",
                description = "REST API to manage and prioritise vulnerabilities (Proyecto Intermodular 2DAM)"),
        security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {
}
