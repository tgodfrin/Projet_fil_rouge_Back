package com.locmns.dto;

import com.locmns.enums.StatusEquipmentType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatusEquipmentRequest {

    @NotNull(message = "Le type de statut est obligatoire")
    private StatusEquipmentType statusEquipmentType;

    // Optionnel : peut être null si aucune description n'est fournie.
    private String descriptionStatus;

    @NotNull(message = "L'équipement est obligatoire")
    private Integer equipmentId;
}
