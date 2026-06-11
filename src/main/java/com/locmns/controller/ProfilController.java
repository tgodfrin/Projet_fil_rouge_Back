package com.locmns.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.locmns.dao.ProfilDao;
import com.locmns.model.Profil;
import com.locmns.view.AppUserView;
import com.locmns.security.IsGestionnaire;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Profils", description = "Profils/rôles disponibles (GESTIONNAIRE, COLLABORATEUR, INTERVENANT, STAGIAIRE).")
public class ProfilController {

    private final ProfilDao profilDao;

    // Alimente le formulaire de création d'utilisateur.
    @Operation(summary = "Lister les profils", description = "Alimente le formulaire de création d'utilisateur. Gestionnaire uniquement.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des profils"),
            @ApiResponse(responseCode = "403", description = "Réservé au gestionnaire")
    })
    @IsGestionnaire
    @GetMapping("/profil/list")
    @JsonView(AppUserView.class)
    public List<Profil> getAll() {
        return profilDao.findAll();
    }
}
