package com.locmns.service;

import com.locmns.dao.LoanDao;
import com.locmns.enums.StatusLoanType;
import com.locmns.model.AppUser;
import com.locmns.model.Loan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanDao loanDao;
    
    public List<Loan> findAll() {
        return loanDao.findAll();
    }

    public Optional<Loan> findById(Integer id) {
        return loanDao.findById(id);
    }

    public List<Loan> findByRequester(Integer userId) {
        AppUser user = new AppUser();
        user.setId(userId);
        return loanDao.findByRequester(user);
    }

    public void create(Loan loan) {
        loan.setId(null);
        // IN_PROGRESS = demande envoyée, en attente de traitement par le gestionnaire
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
}
