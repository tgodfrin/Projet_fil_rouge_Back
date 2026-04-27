package com.locmns.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.locmns.model.Equipment;
import com.locmns.service.EquipmentService;
import com.locmns.view.EquipmentView;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;

    @GetMapping("/equipment/list")
    @JsonView(EquipmentView.class)
    public List<Equipment> getAll() {
        return equipmentService.findAll();
    }


    @GetMapping("/equipment/{id}")
    @JsonView(EquipmentView.class)
    public ResponseEntity<Equipment> getById(@PathVariable Integer id) {
        Optional<Equipment> opt = equipmentService.findById(id);
        if (opt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(opt.get(), HttpStatus.OK);
    }

    @GetMapping("/equipment/available")
    @JsonView(EquipmentView.class)
    public List<Equipment> getAvailable(
            @RequestParam LocalDateTime begin,
            @RequestParam LocalDateTime end) {
        return equipmentService.findAvailableForPeriod(begin, end);
    }

    @PostMapping("/equipment")
    @JsonView(EquipmentView.class)
    public ResponseEntity<Equipment> create(@RequestBody @Valid Equipment equipment) {
        equipmentService.create(equipment);
        return new ResponseEntity<>(equipment, HttpStatus.CREATED);
    }

    @PutMapping("/equipment/{id}")
    public ResponseEntity<Void> update(
            @PathVariable Integer id,
            @RequestBody @Valid Equipment equipment) {
        try {
            equipmentService.update(id, equipment);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (EquipmentService.EquipmentNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/equipment/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            equipmentService.delete(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (EquipmentService.EquipmentNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
