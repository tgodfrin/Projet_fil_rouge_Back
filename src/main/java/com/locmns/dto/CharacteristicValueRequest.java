package com.locmns.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CharacteristicValueRequest {

    @NotBlank(message = "La valeur ne peut pas être vide")
    private String value;

    @NotNull(message = "La caractéristique est obligatoire")
    private Integer characteristicId;

    @NotNull(message = "L'équipement est obligatoire")
    private Integer equipmentId;
}
