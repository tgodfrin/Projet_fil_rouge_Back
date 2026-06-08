package com.locmns.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class LoanRequest {

    @NotNull(message = "La date de début est obligatoire")
    private LocalDate beginDate;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDate endDate;

    @NotNull(message = "L'équipement est obligatoire")
    private Integer equipmentId;

    // Optionnel : null pour un emprunt individuel, UUID pour une demande groupée.
    private String groupId;
}
