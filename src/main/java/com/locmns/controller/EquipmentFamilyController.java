package com.locmns.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.locmns.dao.EquipmentFamilyDao;
import com.locmns.model.EquipmentFamily;
import com.locmns.view.EquipmentFamilyView;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin
@RequiredArgsConstructor
public class EquipmentFamilyController {

    private final EquipmentFamilyDao equipmentFamilyDao;

    @GetMapping("/equipment-family/list")
    @JsonView(EquipmentFamilyView.class)
    public List<EquipmentFamily> getAll() {
        return equipmentFamilyDao.findAll();
    }

    @GetMapping("/equipment-family/{id}")
    @JsonView(EquipmentFamilyView.class)
    public ResponseEntity<EquipmentFamily> getById(@PathVariable Integer id) {
        Optional<EquipmentFamily> opt = equipmentFamilyDao.findById(id);
        if (opt.isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(opt.get(), HttpStatus.OK);
    }

    @PostMapping("/equipment-family")
    @JsonView(EquipmentFamilyView.class)
    public ResponseEntity<EquipmentFamily> create(@RequestBody @Valid EquipmentFamily family) {
        family.setId(null); // on ignore l'id envoyé
        equipmentFamilyDao.save(family);
        return new ResponseEntity<>(family, HttpStatus.CREATED);
    }

    @PutMapping("/equipment-family/{id}")
    public ResponseEntity<Void> update(@PathVariable Integer id, @RequestBody @Valid EquipmentFamily family) {
        Optional<EquipmentFamily> opt = equipmentFamilyDao.findById(id);
        if (opt.isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        family.setId(id);
        equipmentFamilyDao.save(family);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/equipment-family/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        Optional<EquipmentFamily> opt = equipmentFamilyDao.findById(id);
        if (opt.isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        equipmentFamilyDao.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
