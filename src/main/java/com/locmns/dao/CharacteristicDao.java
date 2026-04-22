package com.locmns.dao;

import com.locmns.model.Characteristic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CharacteristicDao extends JpaRepository<Characteristic, Integer> {
}