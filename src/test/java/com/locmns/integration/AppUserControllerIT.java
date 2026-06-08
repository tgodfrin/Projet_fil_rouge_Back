package com.locmns.integration;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithUserDetails;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests d'intégration de la gestion des utilisateurs :
 * contrôle d'accès par rôle (TI07), non-exposition du mot de passe,
 * doublon d'email (TS01) et changement de mot de passe avec ancien erroné (TS02).
 */
class AppUserControllerIT extends AbstractIT {

    @Test
    @WithUserDetails(COLLABORATEUR_EMAIL)
    void liste_utilisateurs_par_collaborateur_renvoie_403() throws Exception {
        mvc.perform(get("/user/list"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(GESTIONNAIRE_EMAIL)
    void liste_utilisateurs_par_gestionnaire_renvoie_200_sans_mot_de_passe() throws Exception {
        mvc.perform(get("/user/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    @WithUserDetails(GESTIONNAIRE_EMAIL)
    void creation_utilisateur_avec_email_existant_renvoie_409() throws Exception {
        // L'email jean.martin@mns.fr est déjà seedé : la contrainte d'unicité doit renvoyer 409.
        String body = "{"
                + "\"email\":\"" + GESTIONNAIRE_EMAIL + "\","
                + "\"name\":\"Doublon\","
                + "\"lastname\":\"Test\","
                + "\"password\":\"motdepasse123\","
                + "\"profilId\":2"
                + "}";
        mvc.perform(post("/user")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    @WithUserDetails(COLLABORATEUR_EMAIL)
    void changement_mot_de_passe_avec_ancien_errone_renvoie_401() throws Exception {
        String body = "{\"oldPassword\":\"mauvais\",\"password\":\"nouveau123\"}";
        mvc.perform(put("/user/me/password")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isUnauthorized());
    }
}
