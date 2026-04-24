package com.locmns.dao;

import com.locmns.enums.StatusLoanType;
import com.locmns.model.AppUser;
import com.locmns.model.Equipment;
import com.locmns.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface LoanDao extends JpaRepository<Loan, Integer> {

    List<Loan> findByRequester(AppUser requester);

    List<Loan> findByStatusType(StatusLoanType statusType);

    List<Loan> findByEndDateBeforeAndStatusType(LocalDateTime date, StatusLoanType statusType);

    List<Loan> findByEquipment(Equipment equipment);

    // Pour la vue planning : emprunts qui chevauchent une période donnée
    List<Loan> findByBeginDateLessThanEqualAndEndDateGreaterThanEqual(LocalDateTime end, LocalDateTime begin);
}