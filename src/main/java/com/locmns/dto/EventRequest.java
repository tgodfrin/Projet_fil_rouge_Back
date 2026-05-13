package com.locmns.dto;

import com.locmns.enums.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventRequest {

    @NotNull(message = "Le type d'événement est obligatoire")
    private EventType type;

    @NotBlank(message = "La description ne peut pas être vide")
    private String description;

    @NotNull(message = "L'emprunt associé est obligatoire")
    private Integer loanId;
}
