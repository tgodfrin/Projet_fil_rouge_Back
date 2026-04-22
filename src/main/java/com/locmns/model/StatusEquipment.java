package com.locmns.model;

import com.locmns.enums.StatusEquipmentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class StatusEquipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Integer id;

    @Column(updatable = false, nullable = false)
    private LocalDateTime beginStatusDate;

    @Column
    private LocalDateTime endStatusDate;

    @Column(columnDefinition = "TEXT")
    protected String descriptionStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusEquipmentType statusEquipmentType;

    @ManyToOne
    @JoinColumn(nullable = false)
    protected Equipment equipment;
}