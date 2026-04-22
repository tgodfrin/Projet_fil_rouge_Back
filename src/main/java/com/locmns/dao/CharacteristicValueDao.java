package com.locmns.dao;

import com.locmns.model.CharacteristicValue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CharacteristicValueDao extends JpaRepository<CharacteristicValue, Integer> {
}