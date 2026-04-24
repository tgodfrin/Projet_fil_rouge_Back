package com.locmns.dao;

import com.locmns.model.Equipment;
import com.locmns.model.EquipmentFamily;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EquipmentDao extends JpaRepository<Equipment, Integer> {

    // Recherche les équipements dont le nom contient la chaîne donnée, sans tenir compte de la casse
    // Containing = LIKE '%...%' | IgnoreCase = insensible à la casse
    List<Equipment> findByEquipmentNameContainingIgnoreCase(String name);

    // Retourne tous les équipements appartenant à une famille donnée
    // Utilisé pour le filtre par catégorie dans le catalogue
    List<Equipment> findByEquipmentFamily(EquipmentFamily equipmentFamily);

    // Retourne les équipements disponibles sur une période donnée (double exclusion)
    // 1er NOT IN : exclut les équipements avec un emprunt actif (non INVALID) qui chevauche la période
    // 2ème NOT IN : exclut les équipements avec un statut technique actif (panne/réparation) qui chevauche la période
    //   → endStatusDate IS NULL signifie que le problème technique est toujours en cours
    // Ce qui reste après les deux exclusions = équipements réellement disponibles
    @Query("""
            SELECT e FROM Equipment e
            WHERE e NOT IN (
                SELECT l.equipment FROM Loan l
                WHERE l.statusType != com.locmns.enums.StatusLoanType.INVALID
                AND l.beginDate < :endDate
                AND l.endDate > :beginDate
            )
            AND e NOT IN (
                SELECT s.equipment FROM StatusEquipment s
                WHERE s.beginStatusDate < :endDate
                AND (s.endStatusDate IS NULL OR s.endStatusDate > :beginDate)
            )
            """)
    List<Equipment> findAvailableEquipments(
            @Param("beginDate") LocalDateTime beginDate,
            @Param("endDate") LocalDateTime endDate
    );
}