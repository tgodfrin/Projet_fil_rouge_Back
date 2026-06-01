package com.locmns.model;

import com.fasterxml.jackson.annotation.JsonView;
import com.locmns.enums.StatusLoanType;
import com.locmns.view.EventView;
import com.locmns.view.LoanView;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
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

    // Date de début souhaitée par le demandeur — fournie à la création (sans heure)
    @Column(nullable = false)
    @NotNull
    @JsonView(LoanView.class)
    private LocalDate beginDate;

    // Date de fin prévue — fournie à la création (sans heure)
    @Column(nullable = false)
    @NotNull
    @JsonView(LoanView.class)
    private LocalDate endDate;

    // Date de retour réelle — null jusqu'au retour effectif du matériel (action "return")
    @Column(nullable = true)
    @JsonView(LoanView.class)
    private LocalDate realEndDate;

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
    @NotNull
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
    @NotNull
    @JsonView(LoanView.class)
    protected Equipment equipment;

    // Groups multiple loan requests submitted together — null for individual loans
    // Generated as UUID by the front when the user selects multiple equipments
    @Column(nullable = true)
    @JsonView(LoanView.class)
    private String groupId;
}