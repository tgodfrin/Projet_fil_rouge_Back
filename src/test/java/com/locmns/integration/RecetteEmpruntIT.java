package com.locmns.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * TEST DE RECETTE — jeu d'essai de la fonctionnalité fil rouge : le processus complet
 * de demande et de validation d'un emprunt, de bout en bout via de vrais tokens JWT.
 *
 * Scénario (section 10 du dossier) :
 *   1. Connexion collaborateur et gestionnaire           -> 200 + JWT
 *   2. Demande d'emprunt par le collaborateur            -> 201 + statut IN_PROGRESS
 *   3. Nouvelle demande chevauchant la même période      -> 409 (équipement indisponible)
 *   4. Validation par le gestionnaire                    -> 204 + statut VALID
 *   5. Enregistrement du retour par le gestionnaire      -> 204 + statut TERMINE + date de retour
 */
class RecetteEmpruntIT extends AbstractIT {

    @Test
    void processus_complet_demande_validation_retour_d_un_emprunt() throws Exception {

        // 1. Connexions : on récupère un JWT pour chaque acteur.
        String jwtCollaborateur = login(COLLABORATEUR_EMAIL, COLLABORATEUR_PASSWORD);
        String jwtGestionnaire = login(GESTIONNAIRE_EMAIL, GESTIONNAIRE_PASSWORD);
        assertThat(jwtCollaborateur.split("\\.")).hasSize(3);
        assertThat(jwtGestionnaire.split("\\.")).hasSize(3);

        // On choisit un équipement existant (le collaborateur peut emprunter toutes les familles).
        String listeJson = mvc.perform(get("/equipment/list")
                        .header("Authorization", bearer(jwtCollaborateur)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        int equipmentId = mapper.readTree(listeJson).get(0).get("id").asInt();

        // 2. Demande d'emprunt sur une période lointaine (aucun conflit avec le jeu de données).
        String demande = "{\"beginDate\":\"2031-03-01\",\"endDate\":\"2031-03-10\",\"equipmentId\":" + equipmentId + "}";
        String empruntCree = mvc.perform(post("/loan")
                        .header("Authorization", bearer(jwtCollaborateur))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(demande))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusType").value("IN_PROGRESS"))
                .andReturn().getResponse().getContentAsString();
        int loanId = mapper.readTree(empruntCree).get("id").asInt();

        // 3. Une demande qui chevauche la même période sur le même équipement est refusée.
        String demandeConflit = "{\"beginDate\":\"2031-03-05\",\"endDate\":\"2031-03-15\",\"equipmentId\":" + equipmentId + "}";
        mvc.perform(post("/loan")
                        .header("Authorization", bearer(jwtCollaborateur))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(demandeConflit))
                .andExpect(status().isConflict());

        // 4. Le gestionnaire valide la demande : elle passe à VALID.
        mvc.perform(put("/loan/" + loanId + "/validate")
                        .header("Authorization", bearer(jwtGestionnaire)))
                .andExpect(status().isNoContent());
        mvc.perform(get("/loan/" + loanId)
                        .header("Authorization", bearer(jwtGestionnaire)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusType").value("VALID"));

        // 5. Le gestionnaire enregistre le retour : statut TERMINE et date de retour réelle renseignée.
        mvc.perform(put("/loan/" + loanId + "/return")
                        .header("Authorization", bearer(jwtGestionnaire)))
                .andExpect(status().isNoContent());
        mvc.perform(get("/loan/" + loanId)
                        .header("Authorization", bearer(jwtGestionnaire)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusType").value("TERMINE"))
                .andExpect(jsonPath("$.realEndDate").exists());

        // Le JsonNode importé garantit que la réponse reste un objet exploitable (pas une boucle infinie).
        JsonNode verification = mapper.readTree(
                mvc.perform(get("/loan/" + loanId).header("Authorization", bearer(jwtGestionnaire)))
                        .andReturn().getResponse().getContentAsString());
        assertThat(verification.get("realEndDate").isNull()).isFalse();
    }
}
