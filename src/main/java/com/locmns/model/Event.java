package com.locmns.model;

import com.fasterxml.jackson.annotation.JsonView;
import com.locmns.enums.EventStatusType;
import com.locmns.enums.EventType;
import com.locmns.view.EventView;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(EventView.class)
    protected Integer id;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    @JsonView(EventView.class)
    private LocalDateTime createdAt;

    // Motif libre saisi par l'utilisateur (anciennement concaténé "date|motif" dans ce champ)
    @Column(columnDefinition = "TEXT")
    @JsonView(EventView.class)
    protected String description;

    // Date demandée par l'utilisateur pour un retour anticipé ou une prolongation — stockée dans un champ dédié
    // (plus de parsing fragile de la description). null pour un incident (BREAKDOWN).
    @Column(nullable = true)
    @JsonView(EventView.class)
    private LocalDate requestedDate;

    // Statut de décision du gestionnaire : PENDING (en attente) → ACCEPTED / REFUSED
    // Permet d'afficher un statut fiable côté utilisateur et de tracer explicitement les refus
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @JsonView(EventView.class)
    private EventStatusType decisionStatus = EventStatusType.PENDING;

    // null = non lu par le gestionnaire ; renseigné = lu
    @Column(nullable = true)
    @JsonView(EventView.class)
    private LocalDateTime readingDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    @JsonView(EventView.class)
    private EventType type;

    // On expose uniquement l'id du Loan pour éviter la boucle infinie Loan→Event→Loan
    // Loan.id est annoté @JsonView({LoanView.class, EventView.class}) dans Loan.java
    @ManyToOne
    @JoinColumn(nullable = false)
    @NotNull
    @JsonView(EventView.class)
    protected Loan loan;

}
