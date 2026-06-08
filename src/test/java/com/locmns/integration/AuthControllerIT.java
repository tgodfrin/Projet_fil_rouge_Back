package com.locmns.integration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests d'intégration de l'authentification (TI01 / TI02).
 */
class AuthControllerIT extends AbstractIT {

    @Test
    void login_avec_identifiants_valides_renvoie_200_et_un_jwt() throws Exception {
        String jwt = mvc.perform(post("/login")
                        .contentType("application/json")
                        .content("{\"email\":\"" + GESTIONNAIRE_EMAIL + "\",\"password\":\"" + GESTIONNAIRE_PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Un JWT est composé de trois segments séparés par des points (header.payload.signature).
        assertThat(jwt).isNotBlank();
        assertThat(jwt.split("\\.")).hasSize(3);
    }

    @Test
    void login_avec_mauvais_mot_de_passe_renvoie_401() throws Exception {
        mvc.perform(post("/login")
                        .contentType("application/json")
                        .content("{\"email\":\"" + GESTIONNAIRE_EMAIL + "\",\"password\":\"mauvais_mot_de_passe\"}"))
                .andExpect(status().isUnauthorized());
    }
}
