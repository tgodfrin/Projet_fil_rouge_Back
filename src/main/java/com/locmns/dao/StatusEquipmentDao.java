package com.locmns.dao;

import com.locmns.model.Equipment;
import com.locmns.model.StatusEquipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface StatusEquipmentDao extends JpaRepository<StatusEquipment, Integer> {

    // Retourne tout l'historique des statuts techniques d'un équipement (pannes, réparations passées et en cours)
    // Utilisé pour la vue détail équipement
    List<StatusEquipment> findByEquipment(Equipment equipment);

    // Retourne le statut technique actuellement actif d'un équipement (endStatusDate IS NULL = toujours en cours)
    // Si résultat vide → pas de problème technique actif sur cet équipement
    List<StatusEquipment> findByEquipmentAndEndStatusDateIsNull(Equipment equipment);

    // Supprime tous les statuts techniques liés à un équipement (utilisé avant suppression de l'équipement)
    void deleteByEquipment(Equipment equipment);

    // Vérifie si un statut technique (panne/réparation) chevauche la période donnée pour un équipement
    // Utilisé par EquipmentService pour calculer le statut OUT_OF_SERVICE/UNDER_REPAIR sur une période (vue gestionnaire)
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