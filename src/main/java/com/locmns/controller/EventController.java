package com.locmns.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.locmns.dto.EventRequest;
import com.locmns.model.Event;
import com.locmns.model.Loan;
import com.locmns.service.EventService;
import com.locmns.view.EventView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Événements", description = "Signalements liés aux emprunts (panne, retour anticipé, prolongation) et leur traitement par le gestionnaire.")
public class EventController {

    private final EventService eventService;

    // Signale un événement (incident, retour anticipé, prolongation) lié à un emprunt.
    // L'utilisateur ne peut signaler que sur ses propres emprunts, sauf gestionnaire.
    @Operation(
            summary = "Signaler un événement",
            description = "Crée un signalement (BREAKDOWN, EARLY_RETURN, EXTENSION) sur un emprunt. "
                    + "On ne peut signaler que sur ses propres emprunts, sauf gestionnaire."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Événement créé"),
            @ApiResponse(responseCode = "403", description = "Signalement sur l'emprunt d'un autre compte interdit"),
            @ApiResponse(responseCode = "404", description = "Emprunt introuvable")
    })
    @IsUser
    @PostMapping("/event")
    @JsonView(EventView.class)
    public ResponseEntity<Event> create(
            @RequestBody @Valid EventRequest dto,
            @AuthenticationPrincipal AppUserDetails userDetails) {
        Optional<Integer> ownerId = eventService.findLoanRequesterId(dto.getLoanId());
        if (ownerId.isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        // Contrôle d'appartenance : on ne crée un signalement que sur son propre emprunt.
        if (!isOwnerOrGestionnaire(userDetails, ownerId.get())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Event saved = eventService.create(toEntity(dto));
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    // Historique des événements d'un emprunt, pour son propriétaire ou un gestionnaire.
    @Operation(summary = "Événements d'un emprunt", description = "Accessible au propriétaire de l'emprunt ou à un gestionnaire.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Événements de l'emprunt"),
            @ApiResponse(responseCode = "403", description = "Emprunt n'appartenant pas à l'appelant"),
            @ApiResponse(responseCode = "404", description = "Emprunt introuvable")
    })
    @IsUser
    @GetMapping("/event/loan/{loanId}")
    @JsonView(EventView.class)
    public ResponseEntity<List<Event>> getByLoan(
            @PathVariable Integer loanId,
            @AuthenticationPrincipal AppUserDetails userDetails) {
        Optional<Integer> ownerId = eventService.findLoanRequesterId(loanId);
        if (ownerId.isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        // Contrôle d'appartenance : seuls le propriétaire de l'emprunt ou un gestionnaire y accèdent.
        if (!isOwnerOrGestionnaire(userDetails, ownerId.get())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(eventService.findByLoan(loanId), HttpStatus.OK);
    }

    // Retours anticipés et prolongations de l'utilisateur connecté,
    // pour afficher ses demandes côté utilisateur.
    @Operation(summary = "Mes événements", description = "Retours anticipés et prolongations de l'utilisateur connecté.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Événements de l'utilisateur connecté")
    })
    @IsUser
    @GetMapping("/event/user")
    @JsonView(EventView.class)
    public List<Event> getMyEvents(@AuthenticationPrincipal AppUserDetails userDetails) {
        return eventService.findByRequester(userDetails.getUser().getId());
    }

    // Tous les événements : le front garde ainsi les incidents lus visibles après navigation.
    @Operation(summary = "Lister tous les événements", description = "Tous les événements (lus et non lus). Gestionnaire uniquement.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tous les événements"),
            @ApiResponse(responseCode = "403", description = "Réservé au gestionnaire")
    })
    @IsGestionnaire
    @GetMapping("/event/list")
    @JsonView(EventView.class)
    public List<Event> getAll() {
        return eventService.findAll();
    }

    // Notifications non lues du gestionnaire.
    @Operation(summary = "Événements non lus", description = "Notifications non lues (readingDate IS NULL). Gestionnaire uniquement.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Événements non lus"),
            @ApiResponse(responseCode = "403", description = "Réservé au gestionnaire")
    })
    @IsGestionnaire
    @GetMapping("/event/unread")
    @JsonView(EventView.class)
    public List<Event> getUnread() {
        return eventService.findUnread();
    }

    // Marque un événement comme lu.
    @Operation(summary = "Marquer un événement comme lu", description = "Renseigne readingDate. Gestionnaire uniquement.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Événement marqué comme lu"),
            @ApiResponse(responseCode = "403", description = "Réservé au gestionnaire"),
            @ApiResponse(responseCode = "404", description = "Événement introuvable")
    })
    @IsGestionnaire
    @PutMapping("/event/{id}/read")
    @JsonView(EventView.class)
    public ResponseEntity<Event> markAsRead(@PathVariable Integer id) {
        Optional<Event> opt = eventService.markAsRead(id);
        if (opt.isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(opt.get(), HttpStatus.OK);
    }

    // Le gestionnaire accepte une demande de retour anticipé ou de prolongation.
    // La décision est tracée et la date de fin de l'emprunt est mise à jour.
    @Operation(
            summary = "Accepter une demande",
            description = "Accepte un retour anticipé ou une prolongation. La décision est tracée et la date de fin de l'emprunt est mise à jour. Gestionnaire uniquement."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Demande acceptée"),
            @ApiResponse(responseCode = "400", description = "Décision invalide pour ce type d'événement"),
            @ApiResponse(responseCode = "403", description = "Réservé au gestionnaire"),
            @ApiResponse(responseCode = "404", description = "Événement introuvable")
    })
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

    // Le gestionnaire refuse une demande de retour anticipé ou de prolongation.
    // Le refus est tracé et l'emprunt reste inchangé.
    @Operation(
            summary = "Refuser une demande",
            description = "Refuse un retour anticipé ou une prolongation. Le refus est tracé et l'emprunt reste inchangé. Gestionnaire uniquement."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Demande refusée"),
            @ApiResponse(responseCode = "403", description = "Réservé au gestionnaire"),
            @ApiResponse(responseCode = "404", description = "Événement introuvable")
    })
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

    // Vrai si l'appelant est gestionnaire ou propriétaire de la ressource.
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
