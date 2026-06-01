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

    @NotNull(message = "Le demandeur est obligatoire")
    private Integer requesterId;

    @NotNull(message = "L'équipement est obligatoire")
    private Integer equipmentId;

    // Optional — null for individual loans, UUID string for grouped loan requests
    private String groupId;
}
