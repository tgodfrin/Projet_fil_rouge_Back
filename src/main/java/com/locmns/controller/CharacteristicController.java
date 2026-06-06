package com.locmns.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.locmns.dao.CharacteristicDao;
import com.locmns.model.Characteristic;
import com.locmns.view.CharacteristicValueView;
import com.locmns.security.IsGestionnaire;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CharacteristicController {

    private final CharacteristicDao characteristicDao;

    // Alimente les listes déroulantes de caractéristiques du formulaire d'équipement.
    @IsGestionnaire
    @GetMapping("/characteristic/list")
    @JsonView(CharacteristicValueView.class)
    public List<Characteristic> getAll() {
        return characteristicDao.findAll();
    }
}
