package com.locmns.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.locmns.dto.StatusEquipmentRequest;
import com.locmns.model.Equipment;
import com.locmns.model.StatusEquipment;
import com.locmns.service.StatusEquipmentService;
import com.locmns.view.StatusEquipmentView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import com.locmns.security.IsGestionnaire;
import com.locmns.security.IsUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Statuts techniques", description = "Pannes et mises en réparation des équipements (en cours ou historique).")
public class StatusEquipmentController {

    private final StatusEquipmentService statusEquipmentService;

    @Operation(summary = "Statuts techniques d'un équipement", description = "Pannes et réparations (en cours et historique) d'un équipement.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statuts techniques de l'équipement"),
            @ApiResponse(responseCode = "404", description = "Équipement introuvable")
    })
    @IsUser
    @GetMapping("/status-equipment/equipment/{equipmentId}")
    @JsonView(StatusEquipmentView.class)
    public ResponseEntity<List<StatusEquipment>> getByEquipment(@PathVariable Integer equipmentId) {
        try {
            List<StatusEquipment> statuses = statusEquipmentService.findByEquipment(equipmentId);
            return new ResponseEntity<>(statuses, HttpStatus.OK);
        } catch (StatusEquipmentService.EquipmentNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Signale une nouvelle panne ou mise en réparation sur un équipement.
    @Operation(summary = "Déclarer une panne ou réparation", description = "Crée un statut technique actif (endStatusDate = null) sur un équipement. Gestionnaire uniquement.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Statut technique créé"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "403", description = "Réservé au gestionnaire")
    })
    @IsGestionnaire
    @PostMapping("/status-equipment")
    @JsonView(StatusEquipmentView.class)
    public ResponseEntity<StatusEquipment> create(@RequestBody @Valid StatusEquipmentRequest dto) {
        StatusEquipment statusEquipment = toEntity(dto);
        statusEquipmentService.create(statusEquipment);
        return new ResponseEntity<>(statusEquipment, HttpStatus.CREATED);
    }

    // Clôture un statut technique (fin de panne ou de réparation).
    @Operation(summary = "Clôturer un statut technique", description = "Renseigne endStatusDate (fin de panne ou de réparation). Gestionnaire uniquement.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Statut technique clôturé"),
            @ApiResponse(responseCode = "403", description = "Réservé au gestionnaire"),
            @ApiResponse(responseCode = "404", description = "Statut technique introuvable")
    })
    @IsGestionnaire
    @PutMapping("/status-equipment/{id}/resolve")
    public ResponseEntity<Void> resolve(@PathVariable Integer id) {
        try {
            statusEquipmentService.resolve(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (StatusEquipmentService.StatusEquipmentNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    private StatusEquipment toEntity(StatusEquipmentRequest dto) {
        StatusEquipment statusEquipment = new StatusEquipment();
        statusEquipment.setStatusEquipmentType(dto.getStatusEquipmentType());
        statusEquipment.setDescriptionStatus(dto.getDescriptionStatus());
        Equipment equipment = new Equipment();
        equipment.setId(dto.getEquipmentId());
        statusEquipment.setEquipment(equipment);
        return statusEquipment;
    }
}
