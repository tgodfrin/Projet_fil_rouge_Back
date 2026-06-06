package com.locmns.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DocRequest {

    @NotBlank(message = "Le titre ne peut pas être vide")
    @Size(min = 3, max = 100, message = "Le titre doit faire entre 3 et 100 caractères")
    private String title;

    @NotBlank(message = "L'URL ne peut pas être vide")
    private String url;

    // Liste optionnelle : un document peut être créé sans équipement lié.
    private List<Integer> equipmentIds;
}
