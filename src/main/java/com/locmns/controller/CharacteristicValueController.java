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

    // Toutes les caractéristiques d'un équipement — utilisé par l'onglet "Caractéristiques" de equipment-detail
    @IsUser
    @GetMapping("/characteristic-value/equipment/{equipmentId}")
    @JsonView(CharacteristicValueView.class)
    public List<CharacteristicValue> getByEquipment(@PathVariable Integer equipmentId) {
        return characteristicValueService.findByEquipment(equipmentId);
    }

    // Créer une valeur de caractéristique et l'associer à un équipement
    // Le front envoie : value, equipmentId, characteristicId
    @IsGestionnaire
    @PostMapping("/characteristic-value")
    @JsonView(CharacteristicValueView.class)
    public ResponseEntity<CharacteristicValue> create(@RequestBody @Valid CharacteristicValueRequest dto) {
        CharacteristicValue saved = characteristicValueService.create(toEntity(dto));
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
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
