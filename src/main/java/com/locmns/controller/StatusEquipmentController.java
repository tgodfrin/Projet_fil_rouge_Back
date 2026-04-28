package com.locmns.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.locmns.model.StatusEquipment;
import com.locmns.service.StatusEquipmentService;
import com.locmns.view.StatusEquipmentView;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequiredArgsConstructor
public class StatusEquipmentController {

    private final StatusEquipmentService statusEquipmentService;

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

    // Signale une nouvelle panne ou mise en réparation sur un équipement
    // Le body doit contenir : statusEquipmentType, descriptionStatus, equipment (avec id)
    // beginStatusDate est rempli automatiquement — endStatusDate restera null jusqu'au resolve
    @PostMapping("/status-equipment")
    @JsonView(StatusEquipmentView.class)
    public ResponseEntity<StatusEquipment> create(@RequestBody @Valid StatusEquipment statusEquipment) {
        statusEquipmentService.create(statusEquipment);
        return new ResponseEntity<>(statusEquipment, HttpStatus.CREATED);
    }

    // Clôture un statut technique (fin de panne ou de réparation)
    // Remplit endStatusDate avec l'heure actuelle — l'équipement redevient disponible
    @PutMapping("/status-equipment/{id}/resolve")
    public ResponseEntity<Void> resolve(@PathVariable Integer id) {
        try {
            statusEquipmentService.resolve(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (StatusEquipmentService.StatusEquipmentNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
