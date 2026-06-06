package com.locmns.service;

import com.locmns.dao.AppUserDao;
import com.locmns.dao.EquipmentDao;
import com.locmns.dao.LoanDao;
import com.locmns.enums.StatusLoanType;
import com.locmns.model.AppUser;
import com.locmns.model.Equipment;
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

    public List<Loan> findAll() {
        return loanDao.findAll();
    }

    public Optional<Loan> findById(Integer id) {
        return loanDao.findById(id);
    }

    // Emprunts validés qui chevauchent une période, pour le planning du gestionnaire.
    // On exclut les demandes en attente et les refus : seuls les emprunts validés apparaissent.
    public List<Loan> findForPlanning(LocalDate begin, LocalDate end) {
        return loanDao.findByStatusTypeAndBeginDateLessThanEqualAndEndDateGreaterThanEqual(
                StatusLoanType.VALID, end, begin);
    }

    public List<Loan> findByRequester(Integer userId) {
        AppUser user = new AppUser();
        user.setId(userId);
        return loanDao.findByRequester(user);
    }

    // Historique complet des emprunts d'un équipement.
    public List<Loan> findByEquipment(Integer equipmentId) {
        Equipment equipment = new Equipment();
        equipment.setId(equipmentId);
        return loanDao.findByEquipment(equipment);
    }

    // Emprunts en retard : validés dont la date de fin est dépassée.
    public List<Loan> findOverdue() {
        return loanDao.findByEndDateBeforeAndStatusType(LocalDate.now(), StatusLoanType.VALID);
    }

    // Demandes en attente de validation.
    public List<Loan> findPending() {
        return loanDao.findByStatusType(StatusLoanType.IN_PROGRESS);
    }

    // La transaction rend atomiques la vérification de disponibilité et l'enregistrement,
    // pour que deux demandes simultanées ne puissent pas réserver le même créneau.
    @Transactional
    public void create(Loan loan) throws UnauthorizedEquipmentFamilyException, EquipmentNotAvailableException {
        loan.setId(null);

        // On recharge le demandeur pour accéder à son profil et aux familles qu'il peut emprunter.
        AppUser requester = appUserDao.findById(loan.getRequester().getId())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // On recharge l'équipement pour connaître sa famille.
        Equipment equipment = equipmentDao.findById(loan.getEquipment().getId())
                .orElseThrow(() -> new RuntimeException("Équipement introuvable"));

        // La famille de l'équipement doit faire partie des familles autorisées du profil.
        // Si le profil n'autorise aucune famille, la demande est refusée.
        boolean isAllowed = requester.getProfil().getEquipmentFamilies().stream()
                .anyMatch(f -> f.getId().equals(equipment.getEquipmentFamily().getId()));

        if (!isAllowed) {
            throw new UnauthorizedEquipmentFamilyException();
        }

        // Aucun emprunt actif ne doit déjà chevaucher la période demandée.
        // Fait dans la transaction, ce contrôle empêche deux demandes simultanées de passer ensemble :
        // la seconde attend la fin de la première, puis échoue sur le conflit déjà enregistré.
        boolean conflict = loanDao.existsByEquipmentAndStatusTypeNotAndBeginDateLessThanAndEndDateGreaterThan(
                equipment,
                StatusLoanType.INVALID,
                loan.getEndDate(),
                loan.getBeginDate()
        );

        if (conflict) {
            throw new EquipmentNotAvailableException();
        }

        // On rattache les entités managées à la place des objets détachés, pour éviter une erreur JPA.
        loan.setRequester(requester);
        loan.setEquipment(equipment);

        loan.setStatusType(StatusLoanType.IN_PROGRESS);
        loan.setStatusDate(LocalDateTime.now());
        loan.setValidator(null);
        loan.setRealEndDate(null);
        loanDao.save(loan);
    }

    // Validation d'une demande par le gestionnaire : elle passe de "en attente" à "validée".
    // getReferenceById renvoie un proxy managé et évite un SELECT inutile.
    public void validate(Integer loanId, Integer validatorId) throws LoanNotFoundException {
        Loan loan = loanDao.findById(loanId).orElseThrow(LoanNotFoundException::new);
        AppUser validator = appUserDao.getReferenceById(validatorId);
        loan.setValidator(validator);
        loan.setStatusType(StatusLoanType.VALID);
        loan.setStatusDate(LocalDateTime.now());
        loanDao.save(loan);
    }

    // Refus d'une demande par le gestionnaire.
    public void invalidate(Integer loanId) throws LoanNotFoundException {
        Loan loan = loanDao.findById(loanId).orElseThrow(LoanNotFoundException::new);
        loan.setStatusType(StatusLoanType.INVALID);
        loan.setStatusDate(LocalDateTime.now());
        loanDao.save(loan);
    }

    // Enregistrement du retour : l'emprunt passe à "terminé" et on note la date de retour réelle.
    // On ne peut enregistrer un retour que sur un emprunt validé : une demande en attente,
    // un emprunt déjà rendu ou un emprunt refusé ne peut pas être rendu.
    public void returnEquipment(Integer loanId) throws LoanNotFoundException, InvalidReturnException {
        Loan loan = loanDao.findById(loanId).orElseThrow(LoanNotFoundException::new);
        if (loan.getStatusType() != StatusLoanType.VALID) {
            throw new InvalidReturnException("Seul un emprunt validé / en cours peut être rendu");
        }
        loan.setStatusType(StatusLoanType.TERMINE);
        loan.setStatusDate(LocalDateTime.now());
        loan.setRealEndDate(LocalDate.now());
        loanDao.save(loan);
    }

    // Emprunts partageant le même groupId.
    public List<Loan> findByGroupId(String groupId) {
        return loanDao.findByGroupId(groupId);
    }

    // Validation de tous les emprunts d'un même groupe en une seule fois.
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

    // Refus de tous les emprunts d'un même groupe en une seule fois.
    public void refuseGroup(String groupId) {
        List<Loan> loans = loanDao.findByGroupId(groupId);
        loans.forEach(loan -> {
            loan.setStatusType(StatusLoanType.INVALID);
            loan.setStatusDate(LocalDateTime.now());
        });
        loanDao.saveAll(loans);
    }

    public static class LoanNotFoundException extends Exception {}

    // Levée quand le profil de l'utilisateur n'autorise pas la famille de l'équipement.
    public static class UnauthorizedEquipmentFamilyException extends Exception {}

    // Levée quand un emprunt actif chevauche déjà la période demandée pour cet équipement.
    public static class EquipmentNotAvailableException extends Exception {}

    // Levée quand on tente un retour sur un emprunt qui n'est pas validé.
    public static class InvalidReturnException extends Exception {
        public InvalidReturnException(String message) { super(message); }
    }
}
