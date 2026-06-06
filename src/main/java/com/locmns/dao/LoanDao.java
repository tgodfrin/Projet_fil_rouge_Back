package com.locmns.dao;

import com.locmns.enums.StatusLoanType;
import com.locmns.model.AppUser;
import com.locmns.model.Equipment;
import com.locmns.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LoanDao extends JpaRepository<Loan, Integer> {

    // Emprunts d'un utilisateur, pour la vue "mes emprunts".
    List<Loan> findByRequester(AppUser requester);

    // Emprunts ayant un statut donné, par exemple les demandes en attente de validation.
    List<Loan> findByStatusType(StatusLoanType statusType);

    // Emprunts dont la date de fin est dépassée pour un statut donné, pour détecter les retards.
    List<Loan> findByEndDateBeforeAndStatusType(LocalDate date, StatusLoanType statusType);

    // Historique des emprunts d'un équipement.
    List<Loan> findByEquipment(Equipment equipment);

    // Emprunts d'un statut donné qui chevauchent une période, utilisé par le planning (filtré sur VALID).
    List<Loan> findByStatusTypeAndBeginDateLessThanEqualAndEndDateGreaterThanEqual(
            StatusLoanType status, LocalDate end, LocalDate begin);

    // Indique si l'utilisateur a au moins un emprunt du statut donné, contrôle effectué avant suppression.
    boolean existsByRequesterAndStatusType(AppUser requester, StatusLoanType statusType);

    // Indique si un emprunt validé a déjà commencé pour cet équipement, pour le calcul du statut sur une date unique.
    boolean existsByEquipmentAndStatusTypeAndBeginDateLessThanEqual(
            Equipment equipment, StatusLoanType statusType, LocalDate date);

    // Supprime les emprunts d'un équipement, avant la suppression de l'équipement lui-même.
    void deleteByEquipment(Equipment equipment);

    // Emprunts partageant le même groupId, pour la validation ou le refus d'un groupe.
    List<Loan> findByGroupId(String groupId);

    // Indique si un emprunt non refusé chevauche déjà la période demandée pour cet équipement.
    // Sert à bloquer les conflits de réservation au moment de la création, dans une transaction.
    boolean existsByEquipmentAndStatusTypeNotAndBeginDateLessThanAndEndDateGreaterThan(
            Equipment equipment,
            StatusLoanType excludedStatus,
            LocalDate requestedEndDate,
            LocalDate requestedBeginDate
    );

    // Indique si un emprunt validé occupe l'équipement sur la période consultée.
    // Un emprunt validé n'est libéré que par son retour effectif : tant qu'il reste VALID il occupe le
    // matériel, même en retard (date de fin dépassée). On le considère occupant si sa période chevauche
    // la fenêtre demandée, ou s'il est en retard (fin antérieure à aujourd'hui alors qu'il n'a pas été rendu).
    @Query("""
            SELECT COUNT(l) > 0 FROM Loan l
            WHERE l.equipment = :equipment
            AND l.statusType = com.locmns.enums.StatusLoanType.VALID
            AND l.beginDate <= :endDate
            AND (l.endDate >= :startDate OR l.endDate < CURRENT_DATE)
            """)
    boolean existsValidLoanOccupyingPeriod(
            @Param("equipment") Equipment equipment,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
