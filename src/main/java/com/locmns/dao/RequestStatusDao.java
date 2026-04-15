package com.locmns.dao;

import com.locmns.model.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestStatusDao extends JpaRepository<RequestStatus, Integer> {
}