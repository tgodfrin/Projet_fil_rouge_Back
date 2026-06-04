package com.locmns.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.locmns.dto.ExtendLoanRequest;
import com.locmns.dto.LoanRequest;
import com.locmns.model.AppUser;
import com.locmns.model.Equipment;
import com.locmns.model.Loan;
import com.locmns.service.LoanService;
import com.locmns.view.LoanView;
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
public class LoanController {

    private final LoanService loanService;

    // Gestionnaire uniquement : voir tous les emprunts
    @IsGestionnaire
    @GetMapping("/loan/list")
    @JsonView(LoanView.class)
    public List<Loan> getAll() {
        return loanService.findAll();
    }

    @IsUser
    @GetMapping("/loan/{id}")
    @JsonView(LoanView.class)
    public ResponseEntity<Loan> getById(@PathVariable Integer id) {
        Optional<Loan> opt = loanService.findById(id);
        if (opt.isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(opt.get(), HttpStatus.OK);
    }

    // Tout utilisateur peut voir ses propres emprunts
    @IsUser
    @GetMapping("/loan/user/{userId}")
    @JsonView(LoanView.class)
    public List<Loan> getByUser(@PathVariable Integer userId) {
        return loanService.findByRequester(userId);
    }

    // Collaborateur peut faire une demande de pret
    // The requester is always taken from the JWT, never from the request body — guarantees traceability
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
            // Le profil de l'utilisateur n'autorise pas la famille de cet équipement
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (LoanService.EquipmentNotAvailableException e) {
            // L'équipement est déjà réservé sur cette période (race condition bloquée côté back)
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    // Planning accessible a tous les roles — accepte des dates ISO YYYY-MM-DD (sans heure)
    @IsUser
    @GetMapping("/loan/planning")
    @JsonView(LoanView.class)
    public List<Loan> getForPlanning(
            @RequestParam LocalDate begin,
            @RequestParam LocalDate end) {
        return loanService.findForPlanning(begin, end);
    }

    @IsUser
    @GetMapping("/loan/equipment/{equipmentId}")
    @JsonView(LoanView.class)
    public List<Loan> getByEquipment(@PathVariable Integer equipmentId) {
        return loanService.findByEquipment(equipmentId);
    }

    // Gestion des retards et demandes en attente : gestionnaire uniquement
    @IsGestionnaire
    @GetMapping("/loan/overdue")
    @JsonView(LoanView.class)
    public List<Loan> getOverdue() {
        return loanService.findOverdue();
    }

    @IsGestionnaire
    @GetMapping("/loan/pending")
    @JsonView(LoanView.class)
    public List<Loan> getPending() {
        return loanService.findPending();
    }

    // Validation/refus : gestionnaire uniquement
    // Le validatorId est lu depuis le token JWT — jamais fourni par le client
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

    // Retour materiel : tout utilisateur authentifie (le collaborateur retourne son materiel)
    @IsUser
    @PutMapping("/loan/{id}/return")
    public ResponseEntity<Void> returnEquipment(@PathVariable Integer id) {
        try {
            loanService.returnEquipment(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (LoanService.LoanNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Gestionnaire validates an early return request — updates endDate to the requested date (stays VALID)
    // The actual TERMINE is triggered separately when the equipment is physically received
    @IsGestionnaire
    @PutMapping("/loan/{id}/validate-early-return")
    @JsonView(LoanView.class)
    public ResponseEntity<Loan> validateEarlyReturn(
            @PathVariable Integer id,
            @RequestBody @Valid ExtendLoanRequest dto) {
        try {
            Loan updated = loanService.validateEarlyReturn(id, dto.getNewEndDate());
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (LoanService.LoanNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Gestionnaire validates an extension request from the alert list
    // No requester ownership check — gestionnaire has authority to approve any extension
    // Accepts overdue (VALID + past endDate) loans as well as active ones
    @IsGestionnaire
    @PutMapping("/loan/{id}/validate-extension")
    @JsonView(LoanView.class)
    public ResponseEntity<Loan> validateExtension(
            @PathVariable Integer id,
            @RequestBody @Valid ExtendLoanRequest dto) {
        try {
            Loan updated = loanService.validateExtension(id, dto.getNewEndDate());
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (LoanService.LoanNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (LoanService.InvalidExtensionException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Get all loans sharing the same groupId — used by front to display group detail
    @IsUser
    @GetMapping("/loan/group/{groupId}")
    @JsonView(LoanView.class)
    public List<Loan> getByGroup(@PathVariable String groupId) {
        return loanService.findByGroupId(groupId);
    }

    // Validate all loans in a group at once
    @IsGestionnaire
    @PutMapping("/loan/group/{groupId}/validate")
    public ResponseEntity<Void> validateGroup(
            @PathVariable String groupId,
            @AuthenticationPrincipal AppUserDetails userDetails) {
        loanService.validateGroup(groupId, userDetails.getUser().getId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // Refuse all loans in a group at once
    @IsGestionnaire
    @PutMapping("/loan/group/{groupId}/refuse")
    public ResponseEntity<Void> refuseGroup(@PathVariable String groupId) {
        loanService.refuseGroup(groupId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
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
