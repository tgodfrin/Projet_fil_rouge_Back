package com.locmns.model;

import com.fasterxml.jackson.annotation.JsonView;
import com.locmns.enums.StatusEquipmentType;
import com.locmns.view.StatusEquipmentView;
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
public class StatusEquipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(StatusEquipmentView.class)
    protected Integer id;

    // @CreationTimestamp : rempli automatiquement par Hibernate à la création, non modifiable
    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    @JsonView(StatusEquipmentView.class)
    private LocalDateTime beginStatusDate;

    // Null tant que la panne/réparation est en cours — rempli lors du resolve
    @Column
    @JsonView(StatusEquipmentView.class)
    private LocalDateTime endStatusDate;

    @Column(columnDefinition = "TEXT")
    @JsonView(StatusEquipmentView.class)
    protected String descriptionStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @JsonView(StatusEquipmentView.class)
    private StatusEquipmentType statusEquipmentType;

    // On expose l'équipement lié — inclura les champs de Equipment annotés @JsonView(EquipmentView.class)
    // Attention : Equipment contient un @Transient status, mais il sera null ici (pas calculé dans ce contexte)
    @ManyToOne
    @JoinColumn(nullable = false)
    @JsonView(StatusEquipmentView.class)
    protected Equipment equipment;
}