package com.locmns.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.locmns.dto.EventRequest;
import com.locmns.model.Event;
import com.locmns.model.Loan;
import com.locmns.service.EventService;
import com.locmns.view.EventView;
import jakarta.validation.Valid;
import com.locmns.security.AppUserDetails;
import com.locmns.security.IsGestionnaire;
import com.locmns.security.IsUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    // Signaler un événement (incident, retour anticipé, extension) lié à un emprunt
    // Le front envoie : type, description, requestedDate, loanId
    // Un utilisateur ne peut signaler que sur SES propres emprunts (ou gestionnaire)
    @IsUser
    @PostMapping("/event")
    @JsonView(EventView.class)
    public ResponseEntity<Event> create(
            @RequestBody @Valid EventRequest dto,
            @AuthenticationPrincipal AppUserDetails userDetails) {
        Optional<Integer> ownerId = eventService.findLoanRequesterId(dto.getLoanId());
        if (ownerId.isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        // Contrôle d'appartenance (IDOR) : on ne peut créer un signalement que sur son propre emprunt
        if (!isOwnerOrGestionnaire(userDetails, ownerId.get())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Event saved = eventService.create(toEntity(dto));
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    // Historique de tous les événements d'un emprunt précis — propriétaire de l'emprunt ou gestionnaire
    @IsUser
    @GetMapping("/event/loan/{loanId}")
    @JsonView(EventView.class)
    public ResponseEntity<List<Event>> getByLoan(
            @PathVariable Integer loanId,
            @AuthenticationPrincipal AppUserDetails userDetails) {
        Optional<Integer> ownerId = eventService.findLoanRequesterId(loanId);
        if (ownerId.isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        // Contrôle d'appartenance (IDOR) : seuls le propriétaire de l'emprunt ou un gestionnaire y accèdent
        if (!isOwnerOrGestionnaire(userDetails, ownerId.get())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(eventService.findByLoan(loanId), HttpStatus.OK);
    }

    // Retourne les events EARLY_RETURN et EXTENSION du user connecté (via JWT)
    // Utilisé côté user pour afficher ses demandes de retour anticipé et prolongation
    @IsUser
    @GetMapping("/event/user")
    @JsonView(EventView.class)
    public List<Event> getMyEvents(@AuthenticationPrincipal AppUserDetails userDetails) {
        return eventService.findByRequester(userDetails.getUser().getId());
    }

    // All events — allows the front to keep read incidents visible after navigation
    @IsGestionnaire
    @GetMapping("/event/list")
    @JsonView(EventView.class)
    public List<Event> getAll() {
        return eventService.findAll();
    }

    // Notifications non lues du gestionnaire (readingDate IS NULL)
    @IsGestionnaire
    @GetMapping("/event/unread")
    @JsonView(EventView.class)
    public List<Event> getUnread() {
        return eventService.findUnread();
    }

    // Marquer un événement comme lu (renseigne readingDate à maintenant)
    @IsGestionnaire
    @PutMapping("/event/{id}/read")
    @JsonView(EventView.class)
    public ResponseEntity<Event> markAsRead(@PathVariable Integer id) {
        Optional<Event> opt = eventService.markAsRead(id);
        if (opt.isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(opt.get(), HttpStatus.OK);
    }

    // Le gestionnaire accepte une demande de retour anticipé / prolongation
    // La décision est tracée (ACCEPTED) et la date de fin de l'emprunt est mise à jour
    @IsGestionnaire
    @PutMapping("/event/{id}/accept")
    @JsonView(EventView.class)
    public ResponseEntity<Event> accept(@PathVariable Integer id) {
        try {
            return new ResponseEntity<>(eventService.accept(id), HttpStatus.OK);
        } catch (EventService.EventNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (EventService.InvalidDecisionException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Le gestionnaire refuse une demande de retour anticipé / prolongation
    // Le refus est tracé explicitement (REFUSED) — l'emprunt reste inchangé
    @IsGestionnaire
    @PutMapping("/event/{id}/refuse")
    @JsonView(EventView.class)
    public ResponseEntity<Event> refuse(@PathVariable Integer id) {
        try {
            return new ResponseEntity<>(eventService.refuse(id), HttpStatus.OK);
        } catch (EventService.EventNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Retourne true si l'appelant est gestionnaire ou s'il est le propriétaire (ownerId) de la ressource
    private boolean isOwnerOrGestionnaire(AppUserDetails userDetails, Integer ownerId) {
        boolean isGestionnaire = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_GESTIONNAIRE"));
        return isGestionnaire || userDetails.getUser().getId().equals(ownerId);
    }

    private Event toEntity(EventRequest dto) {
        Event event = new Event();
        event.setType(dto.getType());
        event.setDescription(dto.getDescription());
        event.setRequestedDate(dto.getRequestedDate());
        Loan loan = new Loan();
        loan.setId(dto.getLoanId());
        event.setLoan(loan);
        return event;
    }
}
