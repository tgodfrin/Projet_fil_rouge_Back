package com.locmns.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EquipmentFamilyRequest {

    @NotBlank(message = "Le nom de la famille ne peut pas être vide")
    @Size(min = 3, max = 30, message = "Le nom doit faire entre 3 et 30 caractères")
    private String nameEquipmentFamily;
}
