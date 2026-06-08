package com.locmns.integration;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithUserDetails;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests d'intégration des accès au parc matériel (TI03 / TI04 + rôle insuffisant).
 * @WithUserDetails charge un vrai utilisateur seedé via le UserDetailsService.
 */
class EquipmentControllerIT extends AbstractIT {

    @Test
    void liste_equipements_sans_authentification_renvoie_403() throws Exception {
        mvc.perform(get("/equipment/list"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(COLLABORATEUR_EMAIL)
    void liste_equipements_avec_collaborateur_renvoie_200_avec_statut_calcule() throws Exception {
        mvc.perform(get("/equipment/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].status").exists());
    }

    @Test
    @WithUserDetails(COLLABORATEUR_EMAIL)
    void suppression_equipement_par_collaborateur_renvoie_403() throws Exception {
        // Endpoint réservé au gestionnaire : un collaborateur est refusé avant toute action.
        mvc.perform(delete("/equipment/1"))
                .andExpect(status().isForbidden());
    }
}
