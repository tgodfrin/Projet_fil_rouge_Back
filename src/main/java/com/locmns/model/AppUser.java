package com.locmns.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class AppUser {

    public interface OnUpdate {}
    public interface OnCreate {}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Integer id;

    @Column(nullable = false, unique = true)
    @NotBlank(groups = OnCreate.class, message = "L'email ne peut pas être vide")
    @Email(groups = OnCreate.class, message = "L'email est mal formé")
    protected String email;

    @Column(nullable = false)
    @NotBlank(groups = OnCreate.class, message = "Le nom ne peut pas être vide")
    protected String name;

    @Column(nullable = false)
    @NotBlank(groups = OnCreate.class, message = "Le prénom ne peut pas être vide")
    protected String lastname;

    @Column(nullable = false)
    @NotBlank(groups = OnCreate.class, message = "Le mot de passe ne peut pas être vide")
    protected String password;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    protected LocalDateTime createdAt;

    @ManyToOne
    protected Profil profil;
}