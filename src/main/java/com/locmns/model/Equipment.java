package com.locmns.model;

import com.fasterxml.jackson.annotation.JsonView;
import com.locmns.view.EquipmentView;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(EquipmentView.class)
    protected Integer id;

    @Column(length = 20, nullable = false, unique = true)
    @NotBlank
    @Size(min = 3, max = 20)
    @JsonView(EquipmentView.class)
    protected String reference;

    @Column(length = 30, nullable = false, unique = true)
    @NotBlank
    @Size(min = 3, max = 30)
    @JsonView(EquipmentView.class)
    protected String equipmentName;

    @Column(length = 100)
    @Size(min = 3, max = 100)
    @JsonView(EquipmentView.class)
    protected String location;

    @Column
    @JsonView(EquipmentView.class)
    protected LocalDate acquisitionDate;

    @ManyToOne
    @JoinColumn(nullable = false)
    @JsonView(EquipmentView.class)
    protected EquipmentFamily equipmentFamily;

    // Champ calculé — PAS en base de données (@Transient = ignoré par JPA/Hibernate)
    // Valeurs possibles : DISPONIBLE | EN_PRET | OUT_OF_SERVICE | UNDER_REPAIR
    // Rempli par EquipmentService.setCalculatedStatus() avant chaque retour d'API
    @Transient
    @JsonView(EquipmentView.class)
    private String status;
}