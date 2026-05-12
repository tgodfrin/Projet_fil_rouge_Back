package com.locmns.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.locmns.dao.CharacteristicDao;
import com.locmns.model.Characteristic;
import com.locmns.view.CharacteristicValueView;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin
@RequiredArgsConstructor
public class CharacteristicController {

    private final CharacteristicDao characteristicDao;

    // Utilisé par le formulaire de création d'équipement pour alimenter les selects de caractéristiques
    @GetMapping("/characteristic/list")
    @JsonView(CharacteristicValueView.class)
    public List<Characteristic> getAll() {
        return characteristicDao.findAll();
    }
}
