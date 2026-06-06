package com.locmns.dto;

import com.locmns.enums.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EventRequest {

    @NotNull(message = "Le type d'événement est obligatoire")
    private EventType type;

    @NotBlank(message = "La description ne peut pas être vide")
    private String description;

    // Date demandée pour un retour anticipé ou une prolongation ; null pour un incident.
    private LocalDate requestedDate;

    @NotNull(message = "L'emprunt associé est obligatoire")
    private Integer loanId;
}
