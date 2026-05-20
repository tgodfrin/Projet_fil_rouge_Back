package com.locmns.dao;

import com.locmns.model.CharacteristicValue;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CharacteristicValueDao extends JpaRepository<CharacteristicValue, Integer> {

    // Retourne toutes les valeurs de caractéristiques liées à un équipement via la table possede (@ManyToMany)
    List<CharacteristicValue> findByEquipmentsId(Integer equipmentId);

    // Supprime toutes les lignes de la table de jointure possede pour un équipement donné
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM possede WHERE equipment_id = :equipmentId", nativeQuery = true)
    void deleteJoinByEquipmentId(@Param("equipmentId") Integer equipmentId);
}