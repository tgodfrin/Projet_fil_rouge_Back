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

    // Retourne toutes les valeurs de caractéristiques d'un équipement — utilisé par equipment-detail
    public List<CharacteristicValue> findByEquipment(Integer equipmentId) {
        return characteristicValueDao.findByEquipmentsId(equipmentId);
    }

    public Optional<CharacteristicValue> findById(Integer id) {
        return characteristicValueDao.findById(id);
    }

    // Crée une valeur de caractéristique et l'associe aux équipements envoyés
    public CharacteristicValue create(CharacteristicValue characteristicValue) {
        return characteristicValueDao.save(characteristicValue);
    }

    // Met à jour uniquement la valeur texte d'une caractéristique existante
    public CharacteristicValue save(CharacteristicValue characteristicValue) {
        return characteristicValueDao.save(characteristicValue);
    }

    // Supprime une valeur de caractéristique par son id
    public void delete(Integer id) {
        characteristicValueDao.deleteById(id);
    }
}
