package com.locmns.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.locmns.model.Loan;
import com.locmns.service.LoanService;
import com.locmns.view.LoanView;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        if (opt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(opt.get(), HttpStatus.OK);
    }


    @GetMapping("/loan/user/{userId}")
    @JsonView(LoanView.class)
    public List<Loan> getByUser(@PathVariable Integer userId) {
        return loanService.findByRequester(userId);
    }


    // Le front envoie : beginDate, endDate, requester: {id}, equipment: {id}
    // statusType, statusDate, validator et realEndDate sont gérés par le service — ne pas les envoyer
    @PostMapping("/loan")
    @JsonView(LoanView.class)
    public ResponseEntity<Loan> create(@RequestBody @Valid Loan loan) {
        loanService.create(loan);
        return new ResponseEntity<>(loan, HttpStatus.CREATED);
    }

    // PUT /loan/{id}/validate?validatorId=X → gestionnaire valide : VALID → IN_PROGRESS
    // validatorId passé en @RequestParam car c'est une donnée simple, pas besoin d'un body entier
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

    // PUT /loan/{id}/invalidate → gestionnaire refuse : VALID → INVALID
    // Pas de body ni de @RequestParam — l'id dans l'URL suffit
    @PutMapping("/loan/{id}/invalidate")
    public ResponseEntity<Void> invalidate(@PathVariable Integer id) {
        try {
            loanService.invalidate(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (LoanService.LoanNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // PUT /loan/{id}/return → retour du matériel : IN_PROGRESS → TERMINE
    // Remplit realEndDate avec now() — marque la fin effective de l'emprunt
    @PutMapping("/loan/{id}/return")
    public ResponseEntity<Void> returnEquipment(@PathVariable Integer id) {
        try {
            loanService.returnEquipment(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (LoanService.LoanNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
