package com.locmns.model;

import com.fasterxml.jackson.annotation.JsonView;
import com.locmns.view.CharacteristicValueView;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

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
    @JsonView(CharacteristicValueView.class)
    protected Integer id;

    @Column(nullable = false)
    @NotBlank
    @JsonView(CharacteristicValueView.class)
    protected String value;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    @JsonView(CharacteristicValueView.class)
    private LocalDateTime beginDate;

    @Column(nullable = true)
    @JsonView(CharacteristicValueView.class)
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
    @JsonView(CharacteristicValueView.class)
    protected Characteristic characteristic;
}
