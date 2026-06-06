package com.locmns.dao;

import com.locmns.model.Equipment;
import com.locmns.model.StatusEquipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface StatusEquipmentDao extends JpaRepository<StatusEquipment, Integer> {

    // Historique des statuts techniques d'un équipement (pannes et réparations).
    List<StatusEquipment> findByEquipment(Equipment equipment);

    // Statut technique encore ouvert d'un équipement (endStatusDate null). Liste vide si aucun.
    List<StatusEquipment> findByEquipmentAndEndStatusDateIsNull(Equipment equipment);

    // Supprime les statuts techniques d'un équipement, avant la suppression de l'équipement.
    void deleteByEquipment(Equipment equipment);

    // Indique si un statut technique chevauche la période donnée pour un équipement.
    @Query("""
            SELECT COUNT(s) > 0 FROM StatusEquipment s
            WHERE s.equipment = :equipment
            AND s.beginStatusDate <= :endDate
            AND (s.endStatusDate IS NULL OR s.endStatusDate >= :startDate)
            """)
    boolean existsByEquipmentOverlappingPeriod(
            @Param("equipment") Equipment equipment,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}