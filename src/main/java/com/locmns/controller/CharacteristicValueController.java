package com.locmns.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.locmns.model.CharacteristicValue;
import com.locmns.service.CharacteristicValueService;
import com.locmns.view.CharacteristicValueView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequiredArgsConstructor
public class CharacteristicValueController {

    private final CharacteristicValueService characteristicValueService;

    // Toutes les caractéristiques d'un équipement — utilisé par l'onglet "Caractéristiques" de equipment-detail
    // Réponse : [{ id, value, beginDate, endDate, characteristic: { id, name } }]
    @GetMapping("/characteristic-value/equipment/{equipmentId}")
    @JsonView(CharacteristicValueView.class)
    public List<CharacteristicValue> getByEquipment(@PathVariable Integer equipmentId) {
        return characteristicValueService.findByEquipment(equipmentId);
    }

    // Créer une valeur de caractéristique et l'associer à un équipement
    // Le front envoie : value, equipments: [{ id }], characteristic: { id }
    @PostMapping("/characteristic-value")
    @JsonView(CharacteristicValueView.class)
    public ResponseEntity<CharacteristicValue> create(@RequestBody CharacteristicValue characteristicValue) {
        CharacteristicValue saved = characteristicValueService.create(characteristicValue);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }
}
