package com.locmns.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppUserRequest {

    @NotBlank(message = "L'email ne peut pas être vide")
    @Email(message = "L'email est mal formé")
    private String email;

    @NotBlank(message = "Le nom ne peut pas être vide")
    private String name;

    @NotBlank(message = "Le prénom ne peut pas être vide")
    private String lastname;

    @NotBlank(message = "Le mot de passe ne peut pas être vide")
    @Size(min = 8, message = "Le mot de passe doit faire au moins 8 caractères")
    private String password;

    @NotNull(message = "Le profil est obligatoire")
    private Integer profilId;
}
