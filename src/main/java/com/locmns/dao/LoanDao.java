package com.locmns.dao;

import com.locmns.enums.StatusLoanType;
import com.locmns.model.AppUser;
import com.locmns.model.Equipment;
import com.locmns.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface LoanDao extends JpaRepository<Loan, Integer> {

    // Retourne tous les emprunts d'un utilisateur donné (celui qui a fait la demande)
    // Utilisé pour la vue "mes emprunts" côté utilisateur
    List<Loan> findByRequester(AppUser requester);

    // Retourne tous les emprunts ayant un statut précis
    // Ex: findByStatusType(IN_PROGRESS) → tous les emprunts en cours
    List<Loan> findByStatusType(StatusLoanType statusType);

    // Retourne les emprunts dont la date de fin est dépassée ET dont le statut correspond
    // Ex: findByEndDateBeforeAndStatusType(now, IN_PROGRESS) → détection des retards
    List<Loan> findByEndDateBeforeAndStatusType(LocalDateTime date, StatusLoanType statusType);

    // Retourne tout l'historique des emprunts pour un équipement précis
    // Utilisé pour la vue détail équipement
    List<Loan> findByEquipment(Equipment equipment);

    // Retourne tous les emprunts qui chevauchent une période donnée (vue planning)
    // Logique : un emprunt chevauche si son début <= fin de la fenêtre ET sa fin >= début de la fenêtre
    List<Loan> findByBeginDateLessThanEqualAndEndDateGreaterThanEqual(LocalDateTime end, LocalDateTime begin);

    // Même logique de chevauchement mais filtrée sur un statut précis
    // Utilisé par findForPlanning() pour n'afficher que les emprunts VALID dans le planning gestionnaire
    List<Loan> findByStatusTypeAndBeginDateLessThanEqualAndEndDateGreaterThanEqual(
            StatusLoanType status, LocalDateTime end, LocalDateTime begin);

    // Vérifie s'il existe au moins un emprunt avec ce statut pour cet équipement
    // Utilisé par EquipmentService pour calculer le statut EN_PRET (loan IN_PROGRESS actif)
    // exists... est plus léger que find... car renvoie un boolean sans charger l'objet entier
    boolean existsByEquipmentAndStatusType(Equipment equipment, StatusLoanType statusType);

    // Supprime tous les emprunts liés à un équipement (utilisé avant suppression de l'équipement)
    void deleteByEquipment(Equipment equipment);

    // Returns all loans sharing the same groupId — used for group validation/refusal
    List<Loan> findByGroupId(String groupId);

    // Vérifie si un emprunt non-INVALID chevauche la période donnée pour un équipement précis
    // Utilisé dans LoanService.create() pour détecter les conflits avant de sauvegarder
    // Condition de chevauchement : beginDate < endDate demandée ET endDate > beginDate demandée
    // statusType != INVALID → on ignore les demandes refusées, elles ne bloquent pas l'équipement
    boolean existsByEquipmentAndStatusTypeNotAndBeginDateLessThanAndEndDateGreaterThan(
            Equipment equipment,
            StatusLoanType excludedStatus,
            LocalDateTime requestedEndDate,
            LocalDateTime requestedBeginDate
    );
}