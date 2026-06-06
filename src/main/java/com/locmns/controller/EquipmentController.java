package com.locmns.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.locmns.dto.EquipmentRequest;
import com.locmns.model.Equipment;
import com.locmns.model.EquipmentFamily;
import com.locmns.service.EquipmentService;
import com.locmns.view.EquipmentView;
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
public class EquipmentController {

    private final EquipmentService equipmentService;

    // Lecture : tout utilisateur connecté peut voir les équipements.
    @IsUser
    @GetMapping("/equipment/list")
    @JsonView(EquipmentView.class)
    public List<Equipment> getAll() {
        return equipmentService.findAll();
    }

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
    @IsUser
    @GetMapping("/equipment/catalogue")
    @JsonView(EquipmentView.class)
    public List<Equipment> getCatalogue(@AuthenticationPrincipal AppUserDetails userDetails) {
        return equipmentService.findForCatalogue(userDetails.getUser().getId());
    }

    // Catalogue disponible sur une période, filtré par profil.
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
