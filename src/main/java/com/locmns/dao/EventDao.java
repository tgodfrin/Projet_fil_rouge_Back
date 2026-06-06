package com.locmns.dao;

import com.locmns.model.Event;
import com.locmns.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventDao extends JpaRepository<Event, Integer> {

    // Événements liés à un emprunt, pour afficher l'historique d'un prêt.
    List<Event> findByLoan(Loan loan);

    // Événements non lus par le gestionnaire (readingDate à null), pour le compteur de notifications.
    List<Event> findByReadingDateIsNull();

    // Supprime les événements liés aux emprunts d'un équipement.
    // À appeler avant de supprimer les emprunts pour respecter la contrainte de clé étrangère.
    @Modifying
    @Query("DELETE FROM Event e WHERE e.loan.equipment.id = :equipmentId")
    void deleteByEquipmentId(@Param("equipmentId") Integer equipmentId);

    // Événements liés aux emprunts d'un utilisateur, du plus récent au plus ancien,
    // pour afficher ses demandes de retour anticipé et de prolongation.
    List<Event> findByLoan_Requester_IdOrderByCreatedAtDesc(Integer requesterId);

    // Supprime les événements liés à une liste d'emprunts, avant la suppression en cascade des emprunts d'un utilisateur.
    @Modifying
    @Query("DELETE FROM Event e WHERE e.loan IN :loans")
    void deleteByLoanIn(@Param("loans") List<Loan> loans);
}
