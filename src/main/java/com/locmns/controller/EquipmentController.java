package com.locmns.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.locmns.dto.EquipmentRequest;
import com.locmns.model.Equipment;
import com.locmns.model.EquipmentFamily;
import com.locmns.service.EquipmentService;
import com.locmns.view.EquipmentView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.locmns.security.AppUserDetails;
import com.locmns.security.IsGestionnaire;
import com.locmns.security.IsUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Tag(name = "Équipements", description = "Gestion du parc matériel et statut calculé (DISPONIBLE, EN_PRET, OUT_OF_SERVICE, UNDER_REPAIR). Catalogue filtré par profil.")
public class EquipmentController {

    private final EquipmentService equipmentService;

    // Lecture : tout utilisateur connecté peut voir les équipements.
    @Operation(summary = "Lister les équipements", description = "Tous les équipements avec leur statut calculé. Tout utilisateur connecté.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des équipements"),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    @IsUser
    @GetMapping("/equipment/list")
    @JsonView(EquipmentView.class)
    public List<Equipment> getAll() {
        return equipmentService.findAll();
    }

    @Operation(summary = "Détail d'un équipement", description = "Tout utilisateur connecté.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Équipement trouvé"),
            @ApiResponse(responseCode = "404", description = "Équipement introuvable")
    })
    @IsUser
    @GetMapping("/equipment/{id}")
    @JsonView(EquipmentView.class)
    public ResponseEntity<Equipment> getById(@PathVariable Integer id) {
        Optional<Equipment> opt = equipmentService.findById(id);
        if (opt.isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(opt.get(), HttpStatus.OK);
    }

    // Catalogue filtré par profil : seuls les équipements des familles autorisées.
    // L'identifiant de l'utilisateur est pris dans le token JWT, jamais fourni par le client.
    @Operation(
            summary = "Catalogue filtré par profil",
            description = "Seuls les équipements des familles autorisées au profil de l'utilisateur connecté (token JWT)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Catalogue autorisé pour le profil")
    })
    @IsUser
    @GetMapping("/equipment/catalogue")
    @JsonView(EquipmentView.class)
    public List<Equipment> getCatalogue(@AuthenticationPrincipal AppUserDetails userDetails) {
        return equipmentService.findForCatalogue(userDetails.getUser().getId());
    }

    // Catalogue disponible sur une période, filtré par profil.
    @Operation(
            summary = "Catalogue disponible sur une période",
            description = "Équipements disponibles entre begin et end, filtrés par profil. Dates au format AAAA-MM-JJ."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Équipements disponibles sur la période"),
            @ApiResponse(responseCode = "400", description = "Dates manquantes ou mal formées")
    })
    @IsUser
    @GetMapping("/equipment/catalogue/available")
    @JsonView(EquipmentView.class)
    public List<Equipment> getCatalogueAvailable(
            @RequestParam LocalDate begin,
            @RequestParam LocalDate end,
            @AuthenticationPrincipal AppUserDetails userDetails) {
        return equipmentService.findAvailableForCatalogue(userDetails.getUser().getId(), begin, end);
    }

    // Tous les équipements avec leur statut calculé sur une date ou une période.
    // endDate est optionnel : si absent, startDate sert aussi de date de fin.
    @Operation(
            summary = "Équipements avec statut à une date",
            description = "Statut calculé sur une date ou une période. endDate est optionnel : si absent, startDate sert de date de fin. Gestionnaire uniquement."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Équipements avec statut pour la période"),
            @ApiResponse(responseCode = "403", description = "Réservé au gestionnaire")
    })
    @IsGestionnaire
    @GetMapping("/equipment/list/by-date")
    @JsonView(EquipmentView.class)
    public List<Equipment> getAllByDate(
            @RequestParam LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        LocalDate end = (endDate != null) ? endDate : startDate;
        return equipmentService.findAllWithStatusForPeriod(startDate, end);
    }

    // Écriture : seuls les gestionnaires gèrent le parc matériel.
    @Operation(summary = "Créer un équipement", description = "Gestionnaire uniquement.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Équipement créé"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "403", description = "Réservé au gestionnaire")
    })
    @IsGestionnaire
    @PostMapping("/equipment")
    @JsonView(EquipmentView.class)
    public ResponseEntity<Equipment> create(@RequestBody @Valid EquipmentRequest dto) {
        Equipment equipment = toEntity(dto);
        equipmentService.create(equipment);
        // On relit depuis la base pour récupérer les relations complètes (famille, etc.).
        Equipment saved = equipmentService.findById(equipment.getId()).orElse(equipment);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @Operation(summary = "Modifier un équipement", description = "Gestionnaire uniquement.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Équipement modifié"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "403", description = "Réservé au gestionnaire"),
            @ApiResponse(responseCode = "404", description = "Équipement introuvable")
    })
    @IsGestionnaire
    @PutMapping("/equipment/{id}")
    public ResponseEntity<Void> update(
            @PathVariable Integer id,
            @RequestBody @Valid EquipmentRequest dto) {
        try {
            equipmentService.update(id, toEntity(dto));
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (EquipmentService.EquipmentNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Supprimer un équipement", description = "Gestionnaire uniquement.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Équipement supprimé"),
            @ApiResponse(responseCode = "403", description = "Réservé au gestionnaire"),
            @ApiResponse(responseCode = "404", description = "Équipement introuvable")
    })
    @IsGestionnaire
    @DeleteMapping("/equipment/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            equipmentService.delete(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (EquipmentService.EquipmentNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    private Equipment toEntity(EquipmentRequest dto) {
        Equipment equipment = new Equipment();
        equipment.setReference(dto.getReference());
        equipment.setEquipmentName(dto.getEquipmentName());
        equipment.setLocation(dto.getLocation());
        equipment.setAcquisitionDate(dto.getAcquisitionDate());
        EquipmentFamily family = new EquipmentFamily();
        family.setId(dto.getEquipmentFamilyId());
        equipment.setEquipmentFamily(family);
        return equipment;
    }
}
