package com.locmns.integration;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithUserDetails;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests d'intégration de contrôle d'accès sur les emprunts.
 * Le cycle de vie complet (création → validation → retour) est couvert par RecetteEmpruntIT.
 */
class LoanControllerIT extends AbstractIT {

    @Test
    @WithUserDetails(COLLABORATEUR_EMAIL)
    void liste_complete_des_emprunts_par_collaborateur_renvoie_403() throws Exception {
        // /loan/list est réservé au gestionnaire (vue globale du parc d'emprunts).
        mvc.perform(get("/loan/list"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(GESTIONNAIRE_EMAIL)
    void liste_complete_des_emprunts_par_gestionnaire_renvoie_200() throws Exception {
        mvc.perform(get("/loan/list"))
                .andExpect(status().isOk());
    }
}
