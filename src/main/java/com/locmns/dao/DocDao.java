package com.locmns.dao;

import com.locmns.model.Doc;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocDao extends JpaRepository<Doc, Integer> {

    // Documents liés à un équipement, via la table fait_reference.
    List<Doc> findByEquipmentsId(Integer equipmentId);

    // Supprime les lignes de la table de jointure fait_reference pour un équipement.
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM fait_reference WHERE equipment_id = :equipmentId", nativeQuery = true)
    void deleteJoinByEquipmentId(@Param("equipmentId") Integer equipmentId);
}