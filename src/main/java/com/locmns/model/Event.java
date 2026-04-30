package com.locmns.model;

import com.fasterxml.jackson.annotation.JsonView;
import com.locmns.enums.EventType;
import com.locmns.view.EventView;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

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

    @Column(columnDefinition = "TEXT")
    @JsonView(EventView.class)
    protected String description;

    // null = non lu par le gestionnaire ; renseigné = lu
    @Column(nullable = true)
    @JsonView(EventView.class)
    private LocalDateTime readingDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @JsonView(EventView.class)
    private EventType type;

    // On expose uniquement l'id du Loan pour éviter la boucle infinie Loan→Event→Loan
    // Loan.id est annoté @JsonView({LoanView.class, EventView.class}) dans Loan.java
    @ManyToOne
    @JoinColumn(nullable = false)
    @JsonView(EventView.class)
    protected Loan loan;

}
