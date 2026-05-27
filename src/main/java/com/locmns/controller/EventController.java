package com.locmns.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.locmns.dto.EventRequest;
import com.locmns.model.Event;
import com.locmns.model.Loan;
import com.locmns.service.EventService;
import com.locmns.view.EventView;
import jakarta.validation.Valid;
import com.locmns.security.IsGestionnaire;
import com.locmns.security.IsUser;
import lombok.RequiredArgsConstructor;
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
    // Le front envoie : type, description, loanId
    @IsUser
    @PostMapping("/event")
    @JsonView(EventView.class)
    public ResponseEntity<Event> create(@RequestBody @Valid EventRequest dto) {
        Event saved = eventService.create(toEntity(dto));
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    // Historique de tous les événements d'un emprunt précis
    @IsUser
    @GetMapping("/event/loan/{loanId}")
    @JsonView(EventView.class)
    public List<Event> getByLoan(@PathVariable Integer loanId) {
        return eventService.findByLoan(loanId);
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

    private Event toEntity(EventRequest dto) {
        Event event = new Event();
        event.setType(dto.getType());
        event.setDescription(dto.getDescription());
        Loan loan = new Loan();
        loan.setId(dto.getLoanId());
        event.setLoan(loan);
        return event;
    }
}
