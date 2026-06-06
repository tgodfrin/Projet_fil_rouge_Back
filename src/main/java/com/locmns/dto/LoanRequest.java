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

    // Ignoré par le serveur : le demandeur est déterminé à partir du token JWT (voir LoanController.create).
    // Laissé optionnel pour rester compatible avec le format envoyé actuellement par le front.
    private Integer requesterId;

    @NotNull(message = "L'équipement est obligatoire")
    private Integer equipmentId;

    // Optionnel : null pour un emprunt individuel, UUID pour une demande groupée.
    private String groupId;
}
