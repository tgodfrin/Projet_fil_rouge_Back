package com.locmns.model;

import com.locmns.enums.EventType;
import com.locmns.enums.StatusEquipmentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

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

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime BeginStatusDate;

    @Column
    private LocalDateTime EndStatusDate;

    @Column(columnDefinition = "TEXT")
    protected String descriptionStatus;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "TEXT")
    private StatusEquipmentType statusEquipmentType;
}
