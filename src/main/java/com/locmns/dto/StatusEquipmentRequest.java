package com.locmns.dto;

import com.locmns.enums.StatusEquipmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatusEquipmentRequest {

    @NotNull(message = "Le type de statut est obligatoire")
    private StatusEquipmentType statusEquipmentType;

    @NotBlank(message = "La description ne peut pas être vide")
    private String descriptionStatus;

    @NotNull(message = "L'équipement est obligatoire")
    private Integer equipmentId;
}
