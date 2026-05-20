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
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @GetMapping("/loan/list")
    @JsonView(LoanView.class)
    public List<Loan> getAll() {
        return loanService.findAll();
    }

    @GetMapping("/loan/{id}")
    @JsonView(LoanView.class)
    public ResponseEntity<Loan> getById(@PathVariable Integer id) {
        Optional<Loan> opt = loanService.findById(id);
        if (opt.isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(opt.get(), HttpStatus.OK);
    }

    @GetMapping("/loan/user/{userId}")
    @JsonView(LoanView.class)
    public List<Loan> getByUser(@PathVariable Integer userId) {
        return loanService.findByRequester(userId);
    }

    // Le front envoie : beginDate, endDate, requesterId, equipmentId
    // statusType, statusDate, validator et realEndDate sont gérés par le service
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

    // GET /loan/planning?begin=...&end=... → tous les loans qui chevauchent la période donnée
    @GetMapping("/loan/planning")
    @JsonView(LoanView.class)
    public List<Loan> getForPlanning(
            @RequestParam LocalDateTime begin,
            @RequestParam LocalDateTime end) {
        return loanService.findForPlanning(begin, end);
    }

    // GET /loan/equipment/{equipmentId} → historique des emprunts d'un équipement
    @GetMapping("/loan/equipment/{equipmentId}")
    @JsonView(LoanView.class)
    public List<Loan> getByEquipment(@PathVariable Integer equipmentId) {
        return loanService.findByEquipment(equipmentId);
    }

    // GET /loan/overdue → emprunts en retard (VALID dont endDate est dépassée)
    @GetMapping("/loan/overdue")
    @JsonView(LoanView.class)
    public List<Loan> getOverdue() {
        return loanService.findOverdue();
    }

    // GET /loan/pending → demandes en attente de validation (IN_PROGRESS)
    @GetMapping("/loan/pending")
    @JsonView(LoanView.class)
    public List<Loan> getPending() {
        return loanService.findPending();
    }

    // PUT /loan/{id}/validate?validatorId=X → gestionnaire valide : IN_PROGRESS → VALID
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

    // PUT /loan/{id}/invalidate → gestionnaire refuse : IN_PROGRESS → INVALID
    @PutMapping("/loan/{id}/invalidate")
    public ResponseEntity<Void> invalidate(@PathVariable Integer id) {
        try {
            loanService.invalidate(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (LoanService.LoanNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // PUT /loan/{id}/return → retour du matériel : VALID → TERMINE
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
