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

    // Crée un événement (incident, retour anticipé ou prolongation) lié à un emprunt.
    // createdAt est rempli par Hibernate, readingDate reste null (non lu) et
    // decisionStatus reste à PENDING tant que le gestionnaire n'a pas décidé.
    public Event create(Event event) {
        return eventDao.save(event);
    }

    /**
     * Acceptation d'une demande de retour anticipé ou de prolongation par le gestionnaire.
     * La décision est tracée (ACCEPTED) et la date de fin de l'emprunt prend la date demandée.
     * L'emprunt reste validé : il ne passe à "terminé" que lors du retour physique du matériel.
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
            // Seul un emprunt validé peut être modifié.
            if (loan.getStatusType() != StatusLoanType.VALID) {
                throw new InvalidDecisionException("Seul un emprunt validé / en cours peut être modifié");
            }
            if (event.getType() == EventType.EARLY_RETURN) {
                // La date de retour anticipé doit être antérieure à la date de fin prévue.
                if (!requested.isBefore(loan.getEndDate())) {
                    throw new InvalidDecisionException("La date de retour anticipé doit être antérieure à la date de fin prévue");
                }
            } else { // EXTENSION
                // La nouvelle date doit être postérieure à la date de fin actuelle.
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
     * Refus d'une demande de retour anticipé ou de prolongation par le gestionnaire.
     * Le refus est tracé (REFUSED) et l'emprunt reste inchangé.
     */
    public Event refuse(Integer eventId) throws EventNotFoundException {
        Event event = eventDao.findById(eventId).orElseThrow(EventNotFoundException::new);
        event.setDecisionStatus(EventStatusType.REFUSED);
        event.setReadingDate(LocalDateTime.now());
        return eventDao.save(event);
    }

    // Id du demandeur de l'emprunt lié, utilisé pour le contrôle d'appartenance.
    public Optional<Integer> findLoanRequesterId(Integer loanId) {
        return loanDao.findById(loanId).map(loan -> loan.getRequester().getId());
    }

    // Tous les événements d'un emprunt, pour afficher son historique.
    public List<Event> findByLoan(Integer loanId) {
        Optional<Loan> loan = loanDao.findById(loanId);
        if (loan.isEmpty()) {
            return List.of();
        }
        return eventDao.findByLoan(loan.get());
    }

    // Tous les événements : la liste d'alertes garde ainsi les incidents lus après une navigation.
    public List<Event> findAll() {
        return eventDao.findAll();
    }

    // Événements non lus, qui alimentent les notifications du gestionnaire.
    public List<Event> findUnread() {
        return eventDao.findByReadingDateIsNull();
    }

    // Événements liés aux emprunts de l'utilisateur connecté.
    // On ne garde que les retours anticipés et prolongations : les incidents sont pour le gestionnaire.
    public List<Event> findByRequester(Integer userId) {
        return eventDao.findByLoan_Requester_IdOrderByCreatedAtDesc(userId)
                .stream()
                .filter(e -> e.getType() == com.locmns.enums.EventType.EARLY_RETURN
                          || e.getType() == com.locmns.enums.EventType.EXTENSION)
                .toList();
    }

    // Marque un événement comme lu en renseignant sa date de lecture.
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

    // Levée quand la décision ne respecte pas les règles métier (date ou statut de l'emprunt).
    public static class InvalidDecisionException extends Exception {
        public InvalidDecisionException(String message) { super(message); }
    }
}
