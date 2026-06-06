package com.locmns.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.locmns.dto.CharacteristicValueRequest;
import com.locmns.model.Characteristic;
import com.locmns.model.CharacteristicValue;
import com.locmns.model.Equipment;
import com.locmns.service.CharacteristicValueService;
import com.locmns.view.CharacteristicValueView;
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
public class CharacteristicValueController {

    private final CharacteristicValueService characteristicValueService;

    // Caractéristiques d'un équipement, pour l'onglet Caractéristiques de la fiche.
    @IsUser
    @GetMapping("/characteristic-value/equipment/{equipmentId}")
    @JsonView(CharacteristicValueView.class)
    public List<CharacteristicValue> getByEquipment(@PathVariable Integer equipmentId) {
        return characteristicValueService.findByEquipment(equipmentId);
    }

    // Crée une valeur de caractéristique et l'associe à un équipement.
    @IsGestionnaire
    @PostMapping("/characteristic-value")
    @JsonView(CharacteristicValueView.class)
    public ResponseEntity<CharacteristicValue> create(@RequestBody @Valid CharacteristicValueRequest dto) {
        CharacteristicValue saved = characteristicValueService.create(toEntity(dto));
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    // Met à jour la valeur d'une caractéristique existante.
    @IsGestionnaire
    @PutMapping("/characteristic-value/{id}")
    @JsonView(CharacteristicValueView.class)
    public ResponseEntity<CharacteristicValue> update(
            @PathVariable Integer id,
            @RequestBody @Valid CharacteristicValueRequest dto) {
        return characteristicValueService.findById(id)
                .map(cv -> {
                    cv.setValue(dto.getValue());
                    return new ResponseEntity<>(characteristicValueService.save(cv), HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Supprime une valeur de caractéristique par son id.
    @IsGestionnaire
    @DeleteMapping("/characteristic-value/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (characteristicValueService.findById(id).isEmpty())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        characteristicValueService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private CharacteristicValue toEntity(CharacteristicValueRequest dto) {
        CharacteristicValue cv = new CharacteristicValue();
        cv.setValue(dto.getValue());
        Characteristic characteristic = new Characteristic();
        characteristic.setId(dto.getCharacteristicId());
        cv.setCharacteristic(characteristic);
        Equipment equipment = new Equipment();
        equipment.setId(dto.getEquipmentId());
        cv.setEquipments(List.of(equipment));
        return cv;
    }
}
