package com.locmns.model;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Integer id;

    @Column(length=30,nullable = false,unique = true)
    @NotBlank
    @Size(min=3,max=30)
    protected String equipmentName;

    @Column(length=100)
    @NotBlank
    @Size(min=3,max=100)
    protected String Location;

    @ManyToOne
    protected StatusEquipment statusEquipment;

    @ManyToOne
    protected  EquipmentFamily equipmentFamily;
}
