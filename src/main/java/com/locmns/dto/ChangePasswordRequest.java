package com.locmns.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

// Corps de requête pour le changement de mot de passe.
// Les identifiants passent désormais par le body JSON (et non en paramètres d'URL)
// pour éviter qu'ils ne finissent dans les journaux serveur / proxys / historique navigateur.
@Getter
@Setter
public class ChangePasswordRequest {

    @NotBlank(message = "L'ancien mot de passe ne peut pas être vide")
    private String oldPassword;

    @NotBlank(message = "Le nouveau mot de passe ne peut pas être vide")
    @Size(min = 8, message = "Le mot de passe doit faire au moins 8 caractères")
    private String password;
}
