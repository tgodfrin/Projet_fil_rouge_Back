package com.locmns.dao;

import com.locmns.model.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EquipmentDao extends JpaRepository<Equipment, Integer> {

    @Query("""
            SELECT e FROM Equipment e
            WHERE e NOT IN (
                SELECT l.equipment FROM Loan l
                WHERE l.requestStatus.statusType != com.locmns.enums.StatusLoanType.INVALID
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