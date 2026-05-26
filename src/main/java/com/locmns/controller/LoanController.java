package com.locmns.controller;

import com.fasterxml.jackson.annotation.JsonView;
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
import com.locmns.security.IsGestionnaire;
import com.locmns.security.IsUser;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
    @IsUser
    @PostMapping("/loan")
    @JsonView(LoanView.class)
    public ResponseEntity<Loan> create(@RequestBody @Valid LoanRequest dto) {
        try {
            Loan loan = toEntity(dto);
            loanService.create(loan);
            return new ResponseEntity<>(loan, HttpStatus.CREATED);
        } catch (LoanService.UnauthorizedEquipmentFamilyException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    // Planning accessible a tous les roles
    @IsUser
    @GetMapping("/loan/planning")
    @JsonView(LoanView.class)
    public List<Loan> getForPlanning(
            @RequestParam LocalDateTime begin,
            @RequestParam LocalDateTime end) {
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
    @IsGestionnaire
    @PutMapping("/loan/{id}/validate")
    public ResponseEntity<Void> validate(
            @PathVariable Integer id,
            @RequestParam Integer validatorId) {
        try {
            loanService.validate(id, validatorId);
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

    private Loan toEntity(LoanRequest dto) {
        Loan loan = new Loan();
        loan.setBeginDate(dto.getBeginDate());
        loan.setEndDate(dto.getEndDate());
        AppUser requester = new AppUser();
        requester.setId(dto.getRequesterId());
        loan.setRequester(requester);
        Equipment equipment = new Equipment();
        equipment.setId(dto.getEquipmentId());
        loan.setEquipment(equipment);
        return loan;
    }
}
