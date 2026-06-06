package com.locmns.dao;

import com.locmns.model.Equipment;
import com.locmns.model.EquipmentFamily;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Collection;

public interface EquipmentDao extends JpaRepository<Equipment, Integer> {

    // Indique si au moins un équipement appartient à la famille, pour empêcher la suppression d'une famille non vide.
    boolean existsByEquipmentFamily(EquipmentFamily equipmentFamily);

    // Équipements appartenant à l'une des familles données, pour le catalogue filtré par profil.
    List<Equipment> findByEquipmentFamilyIn(Collection<EquipmentFamily> families);

    // Équipements disponibles sur une période, dans les familles autorisées.
    // Un équipement est écarté s'il a un emprunt non refusé qui chevauche la période,
    // s'il a un emprunt validé en retard non rendu (toujours sorti), ou s'il a un statut technique actif.
    @Query("""
            SELECT e FROM Equipment e
            WHERE e.equipmentFamily IN :families
            AND e NOT IN (
                SELECT l.equipment FROM Loan l
                WHERE l.statusType <> com.locmns.enums.StatusLoanType.INVALID
                AND l.beginDate < :endDate
                AND (l.endDate > :beginDate
                     OR (l.statusType = com.locmns.enums.StatusLoanType.VALID AND l.endDate < CURRENT_DATE))
            )
            AND e NOT IN (
                SELECT s.equipment FROM StatusEquipment s
                WHERE s.beginStatusDate < :endDate
                AND (s.endStatusDate IS NULL OR s.endStatusDate > :beginDate)
            )
            """)
    List<Equipment> findAvailableEquipmentsInFamilies(
            @Param("beginDate") LocalDateTime beginDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("families") Collection<EquipmentFamily> families
    );
}
