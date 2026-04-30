package com.locmns.model;

import com.fasterxml.jackson.annotation.JsonView;
import com.locmns.enums.StatusLoanType;
import com.locmns.view.EventView;
import com.locmns.view.LoanView;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView({LoanView.class, EventView.class})
    protected Integer id;

    // Date de début souhaitée par le demandeur — fournie à la création
    @Column(nullable = false)
    @JsonView(LoanView.class)
    private LocalDateTime beginDate;

    // Date de fin prévue — fournie à la création
    @Column(nullable = false)
    @JsonView(LoanView.class)
    private LocalDateTime endDate;

    // Date de retour réelle — null jusqu'au retour effectif du matériel (action "return")
    @Column(nullable = true)
    @JsonView(LoanView.class)
    private LocalDateTime realEndDate;

    // Statut actuel du cycle de vie : VALID → IN_PROGRESS → TERMINE (ou INVALID)
    // Géré uniquement par LoanService — jamais modifié directement par le front
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @JsonView(LoanView.class)
    private StatusLoanType statusType;

    // Date du dernier changement de statut — mis à jour à chaque transition
    @Column(nullable = false)
    @JsonView(LoanView.class)
    private LocalDateTime statusDate;

    // L'utilisateur qui a fait la demande — on expose ses champs annotés LoanView (id, name, lastname)
    @ManyToOne
    @JoinColumn(nullable = false)
    @JsonView(LoanView.class)
    protected AppUser requester;

    // Le gestionnaire qui a validé/refusé — null jusqu'à la décision, donc nullable
    @ManyToOne
    @JoinColumn(nullable = true)
    @JsonView(LoanView.class)
    protected AppUser validator;

    // L'équipement emprunté — on expose ses champs annotés LoanView (id, reference, equipmentName)
    @ManyToOne
    @JoinColumn(nullable = false)
    @JsonView(LoanView.class)
    protected Equipment equipment;
}