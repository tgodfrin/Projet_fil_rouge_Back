package com.locmns.model;

import com.fasterxml.jackson.annotation.JsonView;
import com.locmns.view.EquipmentFamilyView;
import com.locmns.view.EquipmentView;
import com.locmns.view.LoanView;
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
public class EquipmentFamily {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView({EquipmentFamilyView.class, EquipmentView.class, LoanView.class})
    protected Integer id;

    @Column(length = 30, nullable = false, unique = true)
    @NotBlank
    @Size(min = 3, max = 30)
    @JsonView({EquipmentFamilyView.class, EquipmentView.class})
    protected String nameEquipmentFamily;
}
