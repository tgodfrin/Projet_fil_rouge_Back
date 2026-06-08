package com.locmns.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

/**
 * Socle commun des tests d'intégration (suffixe IT, lancés par `mvn verify`).
 * Ils tournent sur le PostgreSQL réel seedé par data.sql, comme le projet de référence :
 * on construit un MockMvc à partir du vrai contexte web avec la chaîne de filtres de
 * sécurité (springSecurity()), pour que le JwtFilter et les @PreAuthorize s'appliquent.
 *
 * Identifiants seedés utilisés dans les tests :
 *   - gestionnaire : jean.martin@mns.fr / admin123
 *   - collaborateur : thomas.dupont@mns.fr / user123
 */
@SpringBootTest
abstract class AbstractIT {

    @Autowired
    protected WebApplicationContext context;

    @Autowired
    protected ObjectMapper mapper;

    protected MockMvc mvc;

    protected static final String GESTIONNAIRE_EMAIL = "jean.martin@mns.fr";
    protected static final String GESTIONNAIRE_PASSWORD = "admin123";
    protected static final String COLLABORATEUR_EMAIL = "thomas.dupont@mns.fr";
    protected static final String COLLABORATEUR_PASSWORD = "user123";

    @BeforeEach
    void setupMockMvc() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    // Effectue un vrai POST /login et renvoie le JWT (corps de la réponse).
    protected String login(String email, String password) throws Exception {
        String body = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
        return mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/login")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    protected String bearer(String token) {
        return "Bearer " + token;
    }
}
