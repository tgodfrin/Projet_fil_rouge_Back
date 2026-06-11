package com.locmns.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.locmns.dao.CharacteristicDao;
import com.locmns.model.Characteristic;
import com.locmns.view.CharacteristicValueView;
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
@Tag(name = "Caractéristiques (types)", description = "Types de caractéristiques techniques disponibles pour les équipements.")
public class CharacteristicController {

    private final CharacteristicDao characteristicDao;

    // Alimente les listes déroulantes de caractéristiques du formulaire d'équipement.
    @Operation(summary = "Lister les types de caractéristiques", description = "Alimente les listes déroulantes du formulaire d'équipement. Gestionnaire uniquement.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Types de caractéristiques"),
            @ApiResponse(responseCode = "403", description = "Réservé au gestionnaire")
    })
    @IsGestionnaire
    @GetMapping("/characteristic/list")
    @JsonView(CharacteristicValueView.class)
    public List<Characteristic> getAll() {
        return characteristicDao.findAll();
    }
}
