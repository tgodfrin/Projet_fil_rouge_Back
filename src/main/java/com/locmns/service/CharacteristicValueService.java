package com.locmns.service;

import com.locmns.dao.CharacteristicValueDao;
import com.locmns.model.CharacteristicValue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CharacteristicValueService {

    private final CharacteristicValueDao characteristicValueDao;

    // Valeurs de caractéristiques d'un équipement, pour sa fiche détaillée.
    public List<CharacteristicValue> findByEquipment(Integer equipmentId) {
        return characteristicValueDao.findByEquipmentsId(equipmentId);
    }

    public Optional<CharacteristicValue> findById(Integer id) {
        return characteristicValueDao.findById(id);
    }

    // Crée une valeur de caractéristique et l'associe aux équipements envoyés.
    // On force id à null pour garantir une insertion et empêcher un client d'imposer son identifiant.
    public CharacteristicValue create(CharacteristicValue characteristicValue) {
        characteristicValue.setId(null);
        return characteristicValueDao.save(characteristicValue);
    }

    // Met à jour une valeur de caractéristique existante.
    public CharacteristicValue save(CharacteristicValue characteristicValue) {
        return characteristicValueDao.save(characteristicValue);
    }

    // Supprime une valeur de caractéristique.
    public void delete(Integer id) {
        characteristicValueDao.deleteById(id);
    }
}
