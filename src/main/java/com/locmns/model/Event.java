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

    // Motif libre saisi par l'utilisateur.
    @Column(columnDefinition = "TEXT")
    @JsonView(EventView.class)
    protected String description;

    // Date demandée pour un retour anticipé ou une prolongation ; null pour un incident.
    @Column(nullable = true)
    @JsonView(EventView.class)
    private LocalDate requestedDate;

    // Statut de décision du gestionnaire : en attente, acceptée ou refusée.
    // Donne un statut fiable côté utilisateur et trace explicitement les refus.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @JsonView(EventView.class)
    private EventStatusType decisionStatus = EventStatusType.PENDING;

    // null tant que le gestionnaire n'a pas lu l'événement.
    @Column(nullable = true)
    @JsonView(EventView.class)
    private LocalDateTime readingDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    @JsonView(EventView.class)
    private EventType type;

    // On n'expose que l'id de l'emprunt pour éviter une boucle de sérialisation entre Loan et Event.
    @ManyToOne
    @JoinColumn(nullable = false)
    @NotNull
    @JsonView(EventView.class)
    protected Loan loan;

}
