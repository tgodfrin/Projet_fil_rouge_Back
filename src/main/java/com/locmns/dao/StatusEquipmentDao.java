package com.locmns.dao;

import com.locmns.model.Equipment;
import com.locmns.model.StatusEquipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StatusEquipmentDao extends JpaRepository<StatusEquipment, Integer> {

    // Retourne tout l'historique des statuts techniques d'un équipement (pannes, réparations passées et en cours)
    // Utilisé pour la vue détail équipement
    List<StatusEquipment> findByEquipment(Equipment equipment);

    // Retourne le statut technique actuellement actif d'un équipement (endStatusDate IS NULL = toujours en cours)
    // Si résultat vide → pas de problème technique actif sur cet équipement
    List<StatusEquipment> findByEquipmentAndEndStatusDateIsNull(Equipment equipment);
}