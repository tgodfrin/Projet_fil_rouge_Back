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

    // Returns true if at least one equipment belongs to the given family
    // Used to block deletion of a non-empty family (see EquipmentFamilyController.delete)
    boolean existsByEquipmentFamily(EquipmentFamily equipmentFamily);

    // Retourne tous les équipements appartenant à l'une des familles données
    // Utilisé pour le catalogue filtré par profil : ne montre que les familles autorisées
    List<Equipment> findByEquipmentFamilyIn(Collection<EquipmentFamily> families);

    // Retourne les équipements disponibles sur une période donnée, dans les familles autorisées
    // Combine la logique de dispo + le filtre profil pour le catalogue utilisateur avec dates
    @Query("""
            SELECT e FROM Equipment e
            WHERE e.equipmentFamily IN :families
            AND e NOT IN (
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
    List<Equipment> findAvailableEquipmentsInFamilies(
            @Param("beginDate") LocalDateTime beginDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("families") Collection<EquipmentFamily> families
    );

}