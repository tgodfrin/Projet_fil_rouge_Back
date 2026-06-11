package com.locmns.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.locmns.dto.LoanRequest;
import com.locmns.model.AppUser;
import com.locmns.model.Equipment;
import com.locmns.model.Loan;
import com.locmns.service.LoanService;
import com.locmns.view.LoanView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.locmns.security.AppUserDetails;
import com.locmns.security.IsGestionnaire;
import com.locmns.security.IsUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Tag(name = "Emprunts", description = "Cycle de vie complet d'un emprunt : demande, validation, refus, retour et planning. Fonctionnalité fil rouge.")
public class LoanController {

    private final LoanService loanService;

    // Gestionnaire uniquement : tous les emprunts.
    @Operation(summary = "Lister tous les emprunts", description = "Gestionnaire uniquement.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des emprunts"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Réservé au gestionnaire")
    })
    @IsGestionnaire
    @GetMapping("/loan/list")
    @JsonView(LoanView.class)
    public List<Loan> getAll() {
        return loanService.findAll();
    }

    // Détail d'un emprunt, accessible à son propriétaire ou à un gestionnaire.
    @Operation(summary = "Détail d'un emprunt", description = "Accessible au demandeur de l'emprunt ou à un gestionnaire.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Emprunt trouvé"),
            @ApiResponse(responseCode = "403", description = "Emprunt n'appartenant pas à l'appelant"),
            @ApiResponse(responseCode = "404", description = "Emprunt introuvable")
    })
    @IsUser
    @GetMapping("/loan/{id}")
    @JsonView(LoanView.class)
    public ResponseEntity<Loan> getById(
            @PathVariable Integer id,
            @AuthenticationPrincipal AppUserDetails userDetails) {
        Optional<Loan> opt = loanService.findById(id);
        if (opt.isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        // Contrôle d'appartenance : seul le demandeur ou un gestionnaire peut voir l'emprunt.
        if (!isOwnerOrGestionnaire(userDetails, opt.get().getRequester().getId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(opt.get(), HttpStatus.OK);
    }

    // Emprunts d'un utilisateur : on ne peut consulter que les siens, sauf gestionnaire.
    @Operation(summary = "Emprunts d'un utilisateur", description = "On ne consulte que ses propres emprunts, sauf gestionnaire.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des emprunts de l'utilisateur"),
            @ApiResponse(responseCode = "403", description = "Consultation des emprunts d'un autre compte interdite")
    })
    @IsUser
    @GetMapping("/loan/user/{userId}")
    @JsonView(LoanView.class)
    public ResponseEntity<List<Loan>> getByUser(
            @PathVariable Integer userId,
            @AuthenticationPrincipal AppUserDetails userDetails) {
        // Contrôle d'appartenance : on ne lit les emprunts d'un compte que s'il s'agit du sien.
        if (!isOwnerOrGestionnaire(userDetails, userId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(loanService.findByRequester(userId), HttpStatus.OK);
    }

    // Création d'une demande d'emprunt.
    // Le demandeur est toujours pris dans le token JWT, jamais dans le corps de la requête, pour garantir la traçabilité.
    @Operation(
            summary = "Créer une demande d'emprunt",
            description = "Le demandeur est pris dans le token JWT, jamais dans le corps. "
                    + "L'emprunt est créé au statut IN_PROGRESS."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Demande créée (statut IN_PROGRESS)"),
            @ApiResponse(responseCode = "403", description = "Famille de l'équipement non autorisée pour le profil"),
            @ApiResponse(responseCode = "409", description = "Équipement déjà réservé sur la période demandée")
    })
    @IsUser
    @PostMapping("/loan")
    @JsonView(LoanView.class)
    public ResponseEntity<Loan> create(
            @RequestBody @Valid LoanRequest dto,
            @AuthenticationPrincipal AppUserDetails userDetails) {
        try {
            Loan loan = toEntity(dto, userDetails.getUser().getId());
            loanService.create(loan);
            return new ResponseEntity<>(loan, HttpStatus.CREATED);
        } catch (LoanService.UnauthorizedEquipmentFamilyException e) {
            // Le profil de l'utilisateur n'autorise pas la famille de cet équipement.
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (LoanService.EquipmentNotAvailableException e) {
            // L'équipement est déjà réservé sur cette période (conflit bloqué côté serveur).
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    // Planning accessible à tous les rôles, avec des dates au format AAAA-MM-JJ.
    @Operation(summary = "Planning des emprunts", description = "Emprunts chevauchant la période [begin, end]. Dates au format AAAA-MM-JJ.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Emprunts de la période"),
            @ApiResponse(responseCode = "400", description = "Dates manquantes ou mal formées")
    })
    @IsUser
    @GetMapping("/loan/planning")
    @JsonView(LoanView.class)
    public List<Loan> getForPlanning(
            @RequestParam LocalDate begin,
            @RequestParam LocalDate end) {
        return loanService.findForPlanning(begin, end);
    }

    // Historique des emprunts d'un matériel, avec les noms des emprunteurs (gestionnaire uniquement).
    // C'est une donnée de suivi du parc, pas destinée à l'emprunteur.
    @Operation(summary = "Historique des emprunts d'un équipement", description = "Donnée de suivi du parc. Gestionnaire uniquement.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historique de l'équipement"),
            @ApiResponse(responseCode = "403", description = "Réservé au gestionnaire")
    })
    @IsGestionnaire
    @GetMapping("/loan/equipment/{equipmentId}")
    @JsonView(LoanView.class)
    public List<Loan> getByEquipment(@PathVariable Integer equipmentId) {
        return loanService.findByEquipment(equipmentId);
    }

    // Retards et demandes en attente : gestionnaire uniquement.
    @Operation(summary = "Emprunts en retard", description = "Emprunts validés non rendus dont la date de fin est dépassée. Gestionnaire uniquement.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Emprunts en retard"),
            @ApiResponse(responseCode = "403", description = "Réservé au gestionnaire")
    })
    @IsGestionnaire
    @GetMapping("/loan/overdue")
    @JsonView(LoanView.class)
    public List<Loan> getOverdue() {
        return loanService.findOverdue();
    }

    @Operation(summary = "Demandes d'emprunt en attente", description = "Emprunts au statut IN_PROGRESS à valider ou refuser. Gestionnaire uniquement.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Demandes en attente"),
            @ApiResponse(responseCode = "403", description = "Réservé au gestionnaire")
    })
    @IsGestionnaire
    @GetMapping("/loan/pending")
    @JsonView(LoanView.class)
    public List<Loan> getPending() {
        return loanService.findPending();
    }

    // Validation et refus : gestionnaire uniquement.
    // L'identifiant du valideur est pris dans le token JWT, jamais fourni par le client.
    @Operation(
            summary = "Valider une demande d'emprunt",
            description = "Passe l'emprunt de IN_PROGRESS à VALID. Le valideur est pris dans le token JWT. Gestionnaire uniquement."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Emprunt validé (statut VALID)"),
            @ApiResponse(responseCode = "403", description = "Réservé au gestionnaire"),
            @ApiResponse(responseCode = "404", description = "Emprunt introuvable")
    })
    @IsGestionnaire
    @PutMapping("/loan/{id}/validate")
    public ResponseEntity<Void> validate(
            @PathVariable Integer id,
            @AuthenticationPrincipal AppUserDetails userDetails) {
        try {
            loanService.validate(id, userDetails.getUser().getId());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (LoanService.LoanNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Refuser une demande d'emprunt", description = "Passe l'emprunt de IN_PROGRESS à INVALID. Gestionnaire uniquement.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Emprunt refusé (statut INVALID)"),
            @ApiResponse(responseCode = "403", description = "Réservé au gestionnaire"),
            @ApiResponse(responseCode = "404", description = "Emprunt introuvable")
    })
    @IsGestionnaire
    @PutMapping("/loan/{id}/invalidate")
    public ResponseEntity<Void> invalidate(@PathVariable Integer id) {
        try {
            loanService.invalidate(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (LoanService.LoanNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Enregistrement du retour du matériel : gestionnaire uniquement.
    // L'utilisateur peut seulement demander un retour anticipé, pas enregistrer le retour lui-même.
    @Operation(
            summary = "Enregistrer le retour du matériel",
            description = "Passe l'emprunt de VALID à TERMINE et remplit realEndDate. Gestionnaire uniquement."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Retour enregistré (statut TERMINE)"),
            @ApiResponse(responseCode = "403", description = "Réservé au gestionnaire"),
            @ApiResponse(responseCode = "404", description = "Emprunt introuvable"),
            @ApiResponse(responseCode = "409", description = "Retour impossible : l'emprunt n'est pas au statut VALID")
    })
    @IsGestionnaire
    @PutMapping("/loan/{id}/return")
    public ResponseEntity<Void> returnEquipment(@PathVariable Integer id) {
        try {
            loanService.returnEquipment(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (LoanService.LoanNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (LoanService.InvalidReturnException e) {
            // On ne peut enregistrer un retour que sur un emprunt validé.
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    // La validation et le refus des demandes de retour anticipé et de prolongation passent par
    // EventController (PUT /event/{id}/accept et /refuse), qui tracent la décision.

    // Tous les emprunts d'un même groupe, pour afficher le détail d'un groupe côté front.
    @Operation(summary = "Emprunts d'un groupe", description = "Tous les emprunts partageant le même groupId (emprunt groupé multi-équipements).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Emprunts du groupe")
    })
    @IsUser
    @GetMapping("/loan/group/{groupId}")
    @JsonView(LoanView.class)
    public List<Loan> getByGroup(@PathVariable String groupId) {
        return loanService.findByGroupId(groupId);
    }

    // Valide tous les emprunts d'un groupe en une fois.
    @Operation(summary = "Valider un groupe d'emprunts", description = "Valide en une fois tous les emprunts d'un groupe. Gestionnaire uniquement.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Groupe validé"),
            @ApiResponse(responseCode = "403", description = "Réservé au gestionnaire")
    })
    @IsGestionnaire
    @PutMapping("/loan/group/{groupId}/validate")
    public ResponseEntity<Void> validateGroup(
            @PathVariable String groupId,
            @AuthenticationPrincipal AppUserDetails userDetails) {
        loanService.validateGroup(groupId, userDetails.getUser().getId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // Refuse tous les emprunts d'un groupe en une fois.
    @Operation(summary = "Refuser un groupe d'emprunts", description = "Refuse en une fois tous les emprunts d'un groupe. Gestionnaire uniquement.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Groupe refusé"),
            @ApiResponse(responseCode = "403", description = "Réservé au gestionnaire")
    })
    @IsGestionnaire
    @PutMapping("/loan/group/{groupId}/refuse")
    public ResponseEntity<Void> refuseGroup(@PathVariable String groupId) {
        loanService.refuseGroup(groupId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // Vrai si l'appelant est gestionnaire ou propriétaire de la ressource.
    // Centralise le contrôle d'appartenance qui empêche d'accéder aux données d'autrui.
    private boolean isOwnerOrGestionnaire(AppUserDetails userDetails, Integer ownerId) {
        boolean isGestionnaire = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_GESTIONNAIRE"));
        return isGestionnaire || userDetails.getUser().getId().equals(ownerId);
    }

    private Loan toEntity(LoanRequest dto, Integer requesterId) {
        Loan loan = new Loan();
        loan.setBeginDate(dto.getBeginDate());
        loan.setEndDate(dto.getEndDate());
        AppUser requester = new AppUser();
        requester.setId(requesterId);
        loan.setRequester(requester);
        Equipment equipment = new Equipment();
        equipment.setId(dto.getEquipmentId());
        loan.setEquipment(equipment);
        loan.setGroupId(dto.getGroupId());  // null for individual loans
        return loan;
    }
}
