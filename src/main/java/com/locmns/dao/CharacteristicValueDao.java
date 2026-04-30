package com.locmns.dao;

import com.locmns.model.CharacteristicValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CharacteristicValueDao extends JpaRepository<CharacteristicValue, Integer> {

    // Retourne toutes les valeurs de caractéristiques liées à un équipement via la table possede (@ManyToMany)
    List<CharacteristicValue> findByEquipmentsId(Integer equipmentId);
}