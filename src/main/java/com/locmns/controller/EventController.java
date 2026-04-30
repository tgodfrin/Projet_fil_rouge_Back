package com.locmns.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.locmns.model.Event;
import com.locmns.service.EventService;
import com.locmns.view.EventView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    // Signaler un événement (incident, retour anticipé, extension) lié à un emprunt
    // Le front envoie : type (EventType), description, loan: { id }
    @PostMapping("/event")
    @JsonView(EventView.class)
    public ResponseEntity<Event> create(@RequestBody Event event) {
        Event saved = eventService.create(event);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    // Historique de tous les événements d'un emprunt précis
    @GetMapping("/event/loan/{loanId}")
    @JsonView(EventView.class)
    public List<Event> getByLoan(@PathVariable Integer loanId) {
        return eventService.findByLoan(loanId);
    }

    // Notifications non lues du gestionnaire (readingDate IS NULL)
    @GetMapping("/event/unread")
    @JsonView(EventView.class)
    public List<Event> getUnread() {
        return eventService.findUnread();
    }

    // Marquer un événement comme lu (renseigne readingDate à maintenant)
    @PutMapping("/event/{id}/read")
    @JsonView(EventView.class)
    public ResponseEntity<Event> markAsRead(@PathVariable Integer id) {
        Optional<Event> opt = eventService.markAsRead(id);
        if (opt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(opt.get(), HttpStatus.OK);
    }
}
