package com.locmns.model;

import com.fasterxml.jackson.annotation.JsonView;
import com.locmns.enums.ProfilType;
import com.locmns.view.AppUserView;
import jakarta.persistence.*;
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
public class Profil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(AppUserView.class)
    protected Integer id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    @JsonView(AppUserView.class)
    protected ProfilType type;

    @ManyToMany
    @JoinTable(
            name = "can_loan",
            joinColumns = @JoinColumn(name = "profil_id"),
            inverseJoinColumns = @JoinColumn(name = "equipment_family_id")
    )
    protected List<EquipmentFamily> equipmentFamilies;
}
