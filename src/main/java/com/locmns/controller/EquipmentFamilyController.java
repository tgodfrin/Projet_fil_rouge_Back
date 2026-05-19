package com.locmns.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.locmns.dao.EquipmentFamilyDao;
import com.locmns.model.EquipmentFamily;
import com.locmns.view.EquipmentFamilyView;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequiredArgsConstructor
public class EquipmentFamilyController {

    private final EquipmentFamilyDao equipmentFamilyDao;

    // Les catégories sont fixes — lecture seule uniquement
    @GetMapping("/equipment-family/list")
    @JsonView(EquipmentFamilyView.class)
    public List<EquipmentFamily> getAll() {
        return equipmentFamilyDao.findAll();
    }
}
