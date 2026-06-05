package com.locmns.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

// Corps de requête pour le changement d'email.
// L'email passe par le body JSON (et non en paramètre d'URL) pour ne pas exposer la donnée dans les journaux.
@Getter
@Setter
public class ChangeEmailRequest {

    @NotBlank(message = "L'email ne peut pas être vide")
    @Email(message = "L'email est mal formé")
    private String email;
}
