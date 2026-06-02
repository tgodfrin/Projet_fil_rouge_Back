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

    // Lecture : tout utilisateur authentifie peut voir les equipements
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

    // Catalogue filtré par profil — retourne uniquement les équipements des familles autorisées
    // L'userId est lu depuis le token JWT, jamais fourni par le client
    @IsUser
    @GetMapping("/equipment/catalogue")
    @JsonView(EquipmentView.class)
    public List<Equipment> getCatalogue(@AuthenticationPrincipal AppUserDetails userDetails) {
        return equipmentService.findForCatalogue(userDetails.getUser().getId());
    }

    // Catalogue disponible sur une période, filtré par profil
    @IsUser
    @GetMapping("/equipment/catalogue/available")
    @JsonView(EquipmentView.class)
    public List<Equipment> getCatalogueAvailable(
            @RequestParam LocalDate begin,
            @RequestParam LocalDate end,
            @AuthenticationPrincipal AppUserDetails userDetails) {
        return equipmentService.findAvailableForCatalogue(userDetails.getUser().getId(), begin, end);
    }

    // Accepte des dates ISO YYYY-MM-DD (sans heure) — cohérent avec LocalDate côté Loan
    @IsUser
    @GetMapping("/equipment/available")
    @JsonView(EquipmentView.class)
    public List<Equipment> getAvailable(
            @RequestParam LocalDate begin,
            @RequestParam LocalDate end) {
        return equipmentService.findAvailableForPeriod(begin, end);
    }

    @IsUser
    @GetMapping("/equipment/search")
    @JsonView(EquipmentView.class)
    public List<Equipment> search(@RequestParam String q) {
        return equipmentService.searchByName(q);
    }

    @IsUser
    @GetMapping("/equipment/family/{familyId}")
    @JsonView(EquipmentView.class)
    public List<Equipment> getByFamily(@PathVariable Integer familyId) {
        return equipmentService.findByFamily(familyId);
    }

    // Ecriture : seuls les gestionnaires et admins gerent le parc materiel
    @IsGestionnaire
    @PostMapping("/equipment")
    @JsonView(EquipmentView.class)
    public ResponseEntity<Equipment> create(@RequestBody @Valid EquipmentRequest dto) {
        Equipment equipment = toEntity(dto);
        equipmentService.create(equipment);
        // Reload from DB to get fully populated relations (equipmentFamily, etc.)
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
