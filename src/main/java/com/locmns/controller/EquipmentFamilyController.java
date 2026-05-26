package com.locmns.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.locmns.dao.EquipmentFamilyDao;
import com.locmns.dto.EquipmentFamilyRequest;
import com.locmns.model.EquipmentFamily;
import com.locmns.view.EquipmentFamilyView;
import jakarta.validation.Valid;
import com.locmns.security.IsGestionnaire;
import com.locmns.security.IsUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class EquipmentFamilyController {

    private final EquipmentFamilyDao equipmentFamilyDao;

    // Les catégories sont lisibles par tous les utilisateurs connectés
    @IsUser
    @GetMapping("/equipment-family/list")
    @JsonView(EquipmentFamilyView.class)
    public List<EquipmentFamily> getAll() {
        return equipmentFamilyDao.findAll();
    }

    @IsUser
    @GetMapping("/equipment-family/{id}")
    @JsonView(EquipmentFamilyView.class)
    public ResponseEntity<EquipmentFamily> getById(@PathVariable Integer id) {
        Optional<EquipmentFamily> opt = equipmentFamilyDao.findById(id);
        if (opt.isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(opt.get(), HttpStatus.OK);
    }

    @IsGestionnaire
    @PostMapping("/equipment-family")
    @JsonView(EquipmentFamilyView.class)
    public ResponseEntity<EquipmentFamily> create(@RequestBody @Valid EquipmentFamilyRequest dto) {
        EquipmentFamily family = toEntity(dto);
        equipmentFamilyDao.save(family);
        return new ResponseEntity<>(family, HttpStatus.CREATED);
    }

    @IsGestionnaire
    @PutMapping("/equipment-family/{id}")
    public ResponseEntity<Void> update(@PathVariable Integer id, @RequestBody @Valid EquipmentFamilyRequest dto) {
        if (equipmentFamilyDao.findById(id).isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        EquipmentFamily family = toEntity(dto);
        family.setId(id);
        equipmentFamilyDao.save(family);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @IsGestionnaire
    @DeleteMapping("/equipment-family/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (equipmentFamilyDao.findById(id).isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        equipmentFamilyDao.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private EquipmentFamily toEntity(EquipmentFamilyRequest dto) {
        EquipmentFamily family = new EquipmentFamily();
        family.setNameEquipmentFamily(dto.getNameEquipmentFamily());
        return family;
    }
}
