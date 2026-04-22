package com.locmns.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class CharacteristicValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Integer id;

    @Column(nullable = false)
    @NotBlank
    protected String value;

    @Column(nullable = false, updatable = false)
    private LocalDateTime beginDate;

    @Column(nullable = true)
    private LocalDateTime endDate;

    @ManyToMany
    @JoinTable(
            name = "possede",
            joinColumns = @JoinColumn(name = "characteristic_value_id"),
            inverseJoinColumns = @JoinColumn(name = "equipment_id")
    )
    protected List<Equipment> equipments;

    @ManyToOne
    @JoinColumn(nullable = false)
    protected Characteristic characteristic;
}
