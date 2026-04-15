package com.locmns.dao;

import com.locmns.model.Doc;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocDao extends JpaRepository<Doc, Integer> {
}