package com.locmns.dao;

import com.locmns.model.Equipment;
import com.locmns.model.StatusEquipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StatusEquipmentDao extends JpaRepository<StatusEquipment, Integer> {

    List<StatusEquipment> findByEquipment(Equipment equipment);

    List<StatusEquipment> findByEquipmentAndEndStatusDateIsNull(Equipment equipment);
}