package com.locmns.dao;

import com.locmns.model.Event;
import com.locmns.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventDao extends JpaRepository<Event, Integer> {

    List<Event> findByLoan(Loan loan);
}