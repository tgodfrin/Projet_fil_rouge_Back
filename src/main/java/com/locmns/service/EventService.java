package com.locmns.service;

import com.locmns.dao.EventDao;
import com.locmns.dao.LoanDao;
import com.locmns.model.Event;
import com.locmns.model.Loan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventDao eventDao;
    private final LoanDao loanDao;

    // Crée un événement (incident, retour anticipé, extension) lié à un emprunt existant
    // Le front envoie : type, description, loan: { id }
    // createdAt est géré par Hibernate (@CreationTimestamp), readingDate reste null (non lu)
    public Event create(Event event) {
        return eventDao.save(event);
    }

    // Retourne tous les événements liés à un emprunt — utilisé pour l'historique d'un prêt
    public List<Event> findByLoan(Integer loanId) {
        Optional<Loan> loan = loanDao.findById(loanId);
        if (loan.isEmpty()) {
            return List.of();
        }
        return eventDao.findByLoan(loan.get());
    }

    // Retourne les événements non lus (readingDate IS NULL) — alimente les notifications gestionnaire
    public List<Event> findUnread() {
        return eventDao.findByReadingDateIsNull();
    }

    // Marque un événement comme lu en renseignant sa readingDate à maintenant
    public Optional<Event> markAsRead(Integer id) {
        Optional<Event> opt = eventDao.findById(id);
        if (opt.isEmpty()) {
            return Optional.empty();
        }
        Event event = opt.get();
        event.setReadingDate(LocalDateTime.now());
        eventDao.save(event);
        return Optional.of(event);
    }
}
