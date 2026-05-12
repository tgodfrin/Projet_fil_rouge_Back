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

    // Retourne tous les loans qui chevauchent une période — utilisé par le planning
    public List<Loan> findForPlanning(LocalDateTime begin, LocalDateTime end) {
        return loanDao.findByBeginDateLessThanEqualAndEndDateGreaterThanEqual(end, begin);
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
        return loanDao.findByEndDateBeforeAndStatusType(LocalDateTime.now(), StatusLoanType.VALID);
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

        loan.setStatusType(StatusLoanType.IN_PROGRESS);
        loan.setStatusDate(LocalDateTime.now());
        loan.setValidator(null);
        loan.setRealEndDate(null);
        loanDao.save(loan);
    }

    // Le gestionnaire valide une demande : IN_PROGRESS → VALID
    public void validate(Integer loanId, Integer validatorId) throws LoanNotFoundException {
        Loan loan = loanDao.findById(loanId).orElseThrow(LoanNotFoundException::new);
        AppUser validator = new AppUser();
        validator.setId(validatorId);
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
    // On remplit realEndDate avec l'heure actuelle — c'est la date de retour réelle
    // (peut différer de endDate si retour anticipé ou en retard)
    public void returnEquipment(Integer loanId) throws LoanNotFoundException {
        Loan loan = loanDao.findById(loanId).orElseThrow(LoanNotFoundException::new);
        loan.setStatusType(StatusLoanType.TERMINE);
        loan.setStatusDate(LocalDateTime.now());
        // realEndDate = date effective du retour physique du matériel
        loan.setRealEndDate(LocalDateTime.now());
        loanDao.save(loan);
    }

    public static class LoanNotFoundException extends Exception {}

    // Levée quand le profil de l'utilisateur n'autorise pas la famille de l'équipement demandé
    public static class UnauthorizedEquipmentFamilyException extends Exception {}
}
