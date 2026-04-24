package com.locmns.dao;

import com.locmns.model.Event;
import com.locmns.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventDao extends JpaRepository<Event, Integer> {

    // Retourne tous les événements liés à un emprunt précis (incidents, retours anticipés, extensions)
    // Utilisé pour afficher l'historique d'un prêt
    List<Event> findByLoan(Loan loan);

    // Retourne tous les événements non lus (readingDate IS NULL = pas encore consulté)
    // Utilisé pour alimenter le compteur de notifications du gestionnaire
    List<Event> findByReadingDateIsNull();
}