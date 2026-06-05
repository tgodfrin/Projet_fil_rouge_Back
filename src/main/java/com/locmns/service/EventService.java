package com.locmns.service;

import com.locmns.dao.EventDao;
import com.locmns.dao.LoanDao;
import com.locmns.enums.EventStatusType;
import com.locmns.enums.EventType;
import com.locmns.enums.StatusLoanType;
import com.locmns.model.Event;
import com.locmns.model.Loan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventDao eventDao;
    private final LoanDao loanDao;

    // Crée un événement (incident, retour anticipé, extension) lié à un emprunt existant
    // Le front envoie : type, description (motif), requestedDate, loan: { id }
    // createdAt est géré par Hibernate (@CreationTimestamp), readingDate reste null (non lu),
    // decisionStatus reste PENDING par défaut (en attente de la décision du gestionnaire)
    public Event create(Event event) {
        return eventDao.save(event);
    }

    /**
     * Le gestionnaire accepte une demande de retour anticipé ou de prolongation.
     * La décision est tracée (decisionStatus = ACCEPTED) et la date de fin de l'emprunt
     * est mise à jour avec la date demandée. L'emprunt reste VALID ; le passage à TERMINE
     * se fait séparément quand le matériel est physiquement rendu.
     */
    @Transactional
    public Event accept(Integer eventId) throws EventNotFoundException, InvalidDecisionException {
        Event event = eventDao.findById(eventId).orElseThrow(EventNotFoundException::new);

        if (event.getType() == EventType.EARLY_RETURN || event.getType() == EventType.EXTENSION) {
            Loan loan = loanDao.findById(event.getLoan().getId()).orElseThrow(EventNotFoundException::new);
            LocalDate requested = event.getRequestedDate();

            if (requested == null) {
                throw new InvalidDecisionException("Aucune date demandée sur cette demande");
            }
            // Seul un emprunt validé / en cours peut être modifié
            if (loan.getStatusType() != StatusLoanType.VALID) {
                throw new InvalidDecisionException("Seul un emprunt validé / en cours peut être modifié");
            }
            if (event.getType() == EventType.EARLY_RETURN) {
                // Garde-fou : la date de retour anticipé doit être antérieure à la date de fin prévue
                if (!requested.isBefore(loan.getEndDate())) {
                    throw new InvalidDecisionException("La date de retour anticipé doit être antérieure à la date de fin prévue");
                }
            } else { // EXTENSION
                // Garde-fou : la nouvelle date doit être postérieure à la date de fin actuelle
                if (!requested.isAfter(loan.getEndDate())) {
                    throw new InvalidDecisionException("La nouvelle date doit être postérieure à la date de fin actuelle");
                }
            }
            loan.setEndDate(requested);
            loanDao.save(loan);
        }

        event.setDecisionStatus(EventStatusType.ACCEPTED);
        event.setReadingDate(LocalDateTime.now());
        return eventDao.save(event);
    }

    /**
     * Le gestionnaire refuse une demande de retour anticipé ou de prolongation.
     * Le refus est tracé explicitement (decisionStatus = REFUSED) — l'emprunt reste inchangé.
     */
    public Event refuse(Integer eventId) throws EventNotFoundException {
        Event event = eventDao.findById(eventId).orElseThrow(EventNotFoundException::new);
        event.setDecisionStatus(EventStatusType.REFUSED);
        event.setReadingDate(LocalDateTime.now());
        return eventDao.save(event);
    }

    // Retourne l'id du demandeur (propriétaire) de l'emprunt lié — utilisé pour le contrôle d'appartenance (IDOR)
    public Optional<Integer> findLoanRequesterId(Integer loanId) {
        return loanDao.findById(loanId).map(loan -> loan.getRequester().getId());
    }

    // Retourne tous les événements liés à un emprunt — utilisé pour l'historique d'un prêt
    public List<Event> findByLoan(Integer loanId) {
        Optional<Loan> loan = loanDao.findById(loanId);
        if (loan.isEmpty()) {
            return List.of();
        }
        return eventDao.findByLoan(loan.get());
    }

    // Returns all events — used by the alert list to keep read incidents visible after navigation
    public List<Event> findAll() {
        return eventDao.findAll();
    }

    // Retourne les événements non lus (readingDate IS NULL) — alimente les notifications gestionnaire
    public List<Event> findUnread() {
        return eventDao.findByReadingDateIsNull();
    }

    // Retourne tous les events liés aux loans du user connecté
    // Filtré sur EARLY_RETURN et EXTENSION uniquement (les BREAKDOWN sont pour le gestionnaire)
    public List<Event> findByRequester(Integer userId) {
        return eventDao.findByLoan_Requester_IdOrderByCreatedAtDesc(userId)
                .stream()
                .filter(e -> e.getType() == com.locmns.enums.EventType.EARLY_RETURN
                          || e.getType() == com.locmns.enums.EventType.EXTENSION)
                .toList();
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

    public static class EventNotFoundException extends Exception {}

    // Levée quand la décision sur une demande ne respecte pas les règles métier (date / statut de l'emprunt)
    public static class InvalidDecisionException extends Exception {
        public InvalidDecisionException(String message) { super(message); }
    }
}
