package com.locmns.service;

import com.locmns.dao.CharacteristicValueDao;
import com.locmns.model.CharacteristicValue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CharacteristicValueService {

    private final CharacteristicValueDao characteristicValueDao;

    // Retourne toutes les valeurs de caractéristiques d'un équipement — utilisé par equipment-detail
    public List<CharacteristicValue> findByEquipment(Integer equipmentId) {
        return characteristicValueDao.findByEquipmentsId(equipmentId);
    }

    // Crée une valeur de caractéristique et l'associe aux équipements envoyés (equipments: [{id}])
    // Le front envoie aussi characteristic: { id } pour lier au libellé
    public CharacteristicValue create(CharacteristicValue characteristicValue) {
        return characteristicValueDao.save(characteristicValue);
    }
}
