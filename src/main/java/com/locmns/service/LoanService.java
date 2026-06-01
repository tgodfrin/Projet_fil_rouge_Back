package com.locmns.service;

import com.locmns.dao.AppUserDao;
import com.locmns.dao.EquipmentDao;
import com.locmns.dao.EventDao;
import com.locmns.dao.LoanDao;
import com.locmns.enums.EventType;
import com.locmns.enums.StatusLoanType;
import com.locmns.model.AppUser;
import com.locmns.model.Equipment;
import com.locmns.model.Event;
import com.locmns.model.Loan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanDao      loanDao;
    private final AppUserDao   appUserDao;
    private final EquipmentDao equipmentDao;
    private final EventDao     eventDao;

    public List<Loan> findAll() {
        return loanDao.findAll();
    }

    public Optional<Loan> findById(Integer id) {
        return loanDao.findById(id);
    }

    // Retourne tous les loans VALID qui chevauchent une période — utilisé par le planning gestionnaire
    // Filtre sur VALID : les emprunts IN_PROGRESS (en attente) et INVALID (refusés) ne doivent pas apparaître
    public List<Loan> findForPlanning(LocalDate begin, LocalDate end) {
        return loanDao.findByStatusTypeAndBeginDateLessThanEqualAndEndDateGreaterThanEqual(
                StatusLoanType.VALID, end, begin);
    }

    public List<Loan> findByRequester(Integer userId) {
        AppUser user = new AppUser();
        user.setId(userId);
        return loanDao.findByRequester(user);
    }

    // Retourne tout l'historique des emprunts pour un équipement donné
    public List<Loan> findByEquipment(Integer equipmentId) {
        Equipment equipment = new Equipment();
        equipment.setId(equipmentId);
        return loanDao.findByEquipment(equipment);
    }

    // Retourne les emprunts en retard : VALID dont endDate est dépassée
    public List<Loan> findOverdue() {
        return loanDao.findByEndDateBeforeAndStatusType(LocalDate.now(), StatusLoanType.VALID);
    }

    // Retourne les demandes en attente de validation gestionnaire (IN_PROGRESS)
    public List<Loan> findPending() {
        return loanDao.findByStatusType(StatusLoanType.IN_PROGRESS);
    }

    // @Transactional nécessaire pour charger la collection lazy profil.equipmentFamilies
    @Transactional
    public void create(Loan loan) throws UnauthorizedEquipmentFamilyException {
        loan.setId(null);

        // Chargement complet du demandeur pour accéder à son profil et ses familles autorisées
        AppUser requester = appUserDao.findById(loan.getRequester().getId())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // Chargement complet de l'équipement pour accéder à sa famille
        Equipment equipment = equipmentDao.findById(loan.getEquipment().getId())
                .orElseThrow(() -> new RuntimeException("Équipement introuvable"));

        // Vérifie que la famille de l'équipement est dans les familles autorisées du profil
        // Si la liste est vide, aucune famille n'est autorisée → accès refusé
        boolean isAllowed = requester.getProfil().getEquipmentFamilies().stream()
                .anyMatch(f -> f.getId().equals(equipment.getEquipmentFamily().getId()));

        if (!isAllowed) {
            throw new UnauthorizedEquipmentFamilyException();
        }

        // Remplace les POJOs détachés par les entités managées pour éviter l'erreur JPA
        loan.setRequester(requester);
        loan.setEquipment(equipment);

        loan.setStatusType(StatusLoanType.IN_PROGRESS);
        loan.setStatusDate(LocalDateTime.now());
        loan.setValidator(null);
        loan.setRealEndDate(null);
        loanDao.save(loan);
    }

    // Le gestionnaire valide une demande : IN_PROGRESS → VALID
    // getReferenceById retourne un proxy JPA managé — évite une requête SELECT inutile
    public void validate(Integer loanId, Integer validatorId) throws LoanNotFoundException {
        Loan loan = loanDao.findById(loanId).orElseThrow(LoanNotFoundException::new);
        AppUser validator = appUserDao.getReferenceById(validatorId);
        loan.setValidator(validator);
        loan.setStatusType(StatusLoanType.VALID);
        loan.setStatusDate(LocalDateTime.now());
        loanDao.save(loan);
    }

    // Le gestionnaire refuse une demande : IN_PROGRESS → INVALID
    public void invalidate(Integer loanId) throws LoanNotFoundException {
        Loan loan = loanDao.findById(loanId).orElseThrow(LoanNotFoundException::new);
        loan.setStatusType(StatusLoanType.INVALID);
        loan.setStatusDate(LocalDateTime.now());
        loanDao.save(loan);
    }

    // Retour du matériel : VALID → TERMINE
    // realEndDate = date effective du retour physique (sans heure, cohérent avec beginDate/endDate)
    public void returnEquipment(Integer loanId) throws LoanNotFoundException {
        Loan loan = loanDao.findById(loanId).orElseThrow(LoanNotFoundException::new);
        loan.setStatusType(StatusLoanType.TERMINE);
        loan.setStatusDate(LocalDateTime.now());
        loan.setRealEndDate(LocalDate.now());
        loanDao.save(loan);
    }

    // Returns all loans sharing the same groupId — used by GET /loan/group/:groupId
    public List<Loan> findByGroupId(String groupId) {
        return loanDao.findByGroupId(groupId);
    }

    // Validates all loans sharing the same groupId — gestionnaire approves the whole group at once
    public void validateGroup(String groupId, Integer validatorId) {
        List<Loan> loans = loanDao.findByGroupId(groupId);
        AppUser validator = appUserDao.getReferenceById(validatorId);
        loans.forEach(loan -> {
            loan.setValidator(validator);
            loan.setStatusType(StatusLoanType.VALID);
            loan.setStatusDate(LocalDateTime.now());
        });
        loanDao.saveAll(loans);
    }

    // Refuses all loans sharing the same groupId — gestionnaire rejects the whole group at once
    public void refuseGroup(String groupId) {
        List<Loan> loans = loanDao.findByGroupId(groupId);
        loans.forEach(loan -> {
            loan.setStatusType(StatusLoanType.INVALID);
            loan.setStatusDate(LocalDateTime.now());
        });
        loanDao.saveAll(loans);
    }

    /**
     * Extends a loan's end date.
     * Checks: requester ownership, valid status (VALID or IN_PROGRESS), new date must be after current end date.
     * Creates an EXTENSION event on success.
     */
    public Loan extend(Integer loanId, Integer requesterId, LocalDate newEndDate)
            throws LoanNotFoundException, ForbiddenException, InvalidExtensionException {
        Loan loan = loanDao.findById(loanId).orElseThrow(LoanNotFoundException::new);

        // Only the requester can extend their own loan
        if (!loan.getRequester().getId().equals(requesterId)) {
            throw new ForbiddenException();
        }

        // Only active loans can be extended
        if (loan.getStatusType() != StatusLoanType.VALID
                && loan.getStatusType() != StatusLoanType.IN_PROGRESS) {
            throw new InvalidExtensionException("Seuls les emprunts en cours ou en attente peuvent être prolongés");
        }

        // New date must be strictly after current end date
        if (!newEndDate.isAfter(loan.getEndDate())) {
            throw new InvalidExtensionException("La nouvelle date doit être après la date de fin actuelle");
        }

        loan.setEndDate(newEndDate);
        loanDao.save(loan);

        // Record the extension as an event
        Event event = new Event();
        event.setType(EventType.EXTENSION);
        event.setDescription("Extension jusqu'au " + newEndDate);
        event.setLoan(loan);
        eventDao.save(event);

        return loan;
    }

    public static class LoanNotFoundException extends Exception {}

    // Levée quand le profil de l'utilisateur n'autorise pas la famille de l'équipement demandé
    public static class UnauthorizedEquipmentFamilyException extends Exception {}

    // Levée quand l'utilisateur tente d'agir sur un emprunt qui ne lui appartient pas
    public static class ForbiddenException extends Exception {}

    // Levée quand les règles métier de prolongation ne sont pas respectées
    public static class InvalidExtensionException extends Exception {
        public InvalidExtensionException(String message) { super(message); }
    }
}
