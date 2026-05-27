package com.locmns.dao;

import com.locmns.model.Event;
import com.locmns.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventDao extends JpaRepository<Event, Integer> {

    // Retourne tous les événements liés à un emprunt précis (incidents, retours anticipés, extensions)
    // Utilisé pour afficher l'historique d'un prêt
    List<Event> findByLoan(Loan loan);

    // Retourne tous les événements non lus (readingDate IS NULL = pas encore consulté)
    // Utilisé pour alimenter le compteur de notifications du gestionnaire
    List<Event> findByReadingDateIsNull();

    // Retourne tous les événements liés à un équipement (via ses loans)
    // Utilisé pour alimenter l'onglet alertes du gestionnaire
    List<Event> findByLoan_Equipment_Id(Integer equipmentId);

    // Supprime tous les events liés aux loans d'un équipement donné
    // Must be called before loanDao.deleteByEquipment to avoid FK constraint violation
    @Modifying
    @Query("DELETE FROM Event e WHERE e.loan.equipment.id = :equipmentId")
    void deleteByEquipmentId(@Param("equipmentId") Integer equipmentId);
}