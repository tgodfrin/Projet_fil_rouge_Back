package com.locmns.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger configuration.
 * <p>
 * Declares the global API metadata and a JWT "bearer" security scheme so the
 * Swagger UI exposes an "Authorize" button. Once a token obtained from
 * {@code POST /login} is pasted there, every protected endpoint can be called
 * directly from the UI with the {@code Authorization: Bearer <token>} header.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "LOC MNS API",
                version = "1.0",
                description = "API REST de gestion du parc de matériel MNS : "
                        + "authentification JWT, équipements, familles, emprunts, "
                        + "événements, statuts techniques, documents et utilisateurs.",
                contact = @Contact(name = "MNS - Projet fil rouge CDA")
        ),
        servers = @Server(url = "http://localhost:8080", description = "Environnement de développement local"),
        // Applique le schéma JWT à toutes les opérations par défaut.
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Coller le token JWT renvoyé par POST /login (sans le préfixe 'Bearer ')."
)
public class OpenApiConfig {
}
