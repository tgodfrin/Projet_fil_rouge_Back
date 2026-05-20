package com.locmns.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EquipmentRequest {

    @NotBlank(message = "La référence ne peut pas être vide")
    @Size(min = 3, max = 20, message = "La référence doit faire entre 3 et 20 caractères")
    private String reference;

    @NotBlank(message = "Le nom de l'équipement ne peut pas être vide")
    @Size(min = 3, max = 30, message = "Le nom doit faire entre 3 et 30 caractères")
    private String equipmentName;

    @Size(min = 3, max = 100, message = "La localisation doit faire entre 3 et 100 caractères")
    private String location;

    private LocalDate acquisitionDate;

    @NotNull(message = "La famille d'équipement est obligatoire")
    private Integer equipmentFamilyId;
}
