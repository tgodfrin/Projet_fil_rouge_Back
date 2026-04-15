package com.locmns.dao;

import com.locmns.model.AppUser;
import com.locmns.model.Loan;
import com.locmns.model.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface LoanDao extends JpaRepository<Loan, Integer> {

    List<Loan> findByRequester(AppUser requester);

    List<Loan> findByRequestStatus(RequestStatus requestStatus);

    List<Loan> findByEndDateBeforeAndRequestStatus(LocalDateTime date, RequestStatus requestStatus);
}