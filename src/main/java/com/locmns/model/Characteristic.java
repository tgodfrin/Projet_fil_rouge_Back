package com.locmns.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Characteristic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Integer id;

    @Column(length = 50, nullable = false, unique = true)
    @NotBlank
    @Size(min = 2, max = 50)
    protected String name;

    @ManyToMany
    @JoinTable(
            name = "est_constitue",
            joinColumns = @JoinColumn(name = "caracteristique_id"),
            inverseJoinColumns = @JoinColumn(name = "equipment_family_id")
    )
    protected List<EquipmentFamily> equipmentFamilies;
}
