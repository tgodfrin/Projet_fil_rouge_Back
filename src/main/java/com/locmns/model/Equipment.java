package com.locmns.model;

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
    protected Integer id;

    @Column(length = 20, nullable = false, unique = true)
    @NotBlank
    @Size(min = 3, max = 20)
    protected String reference;

    @Column(length = 30, nullable = false, unique = true)
    @NotBlank
    @Size(min = 3, max = 30)
    protected String equipmentName;

    @Column(length = 100)
    @NotBlank
    @Size(min = 3, max = 100)
    protected String location;

    @Column
    protected LocalDate acquisitionDate;

    @ManyToOne
    @JoinColumn(nullable = false)
    protected EquipmentFamily equipmentFamily;
}