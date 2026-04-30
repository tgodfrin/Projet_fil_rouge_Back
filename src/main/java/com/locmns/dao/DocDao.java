package com.locmns.dao;

import com.locmns.model.Doc;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocDao extends JpaRepository<Doc, Integer> {

    // Retourne tous les docs liés à un équipement via la table fait_reference (@ManyToMany)
    List<Doc> findByEquipmentsId(Integer equipmentId);
}