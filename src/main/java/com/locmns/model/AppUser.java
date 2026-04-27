package com.locmns.model;

import com.fasterxml.jackson.annotation.JsonView;
import com.locmns.view.AppUserView;
import com.locmns.view.LoanView;
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
    @JsonView({AppUserView.class, LoanView.class})
    protected Integer id;

    @Column(nullable = false, unique = true)
    @NotBlank(groups = OnCreate.class, message = "L'email ne peut pas être vide")
    @Email(groups = OnCreate.class, message = "L'email est mal formé")
    @JsonView(AppUserView.class)
    protected String email;

    @Column(nullable = false, updatable = false)
    @NotBlank(groups = OnCreate.class, message = "Le nom ne peut pas être vide")
    @JsonView({AppUserView.class, LoanView.class})
    protected String name;

    @Column(nullable = false, updatable = false)
    @NotBlank(groups = OnCreate.class, message = "Le prénom ne peut pas être vide")
    @JsonView({AppUserView.class, LoanView.class})
    protected String lastname;

    @Column(nullable = false)
    @NotBlank(groups = OnCreate.class, message = "Le mot de passe ne peut pas être vide")
    // pas de @JsonView → le password n'est jamais sérialisé
    protected String password;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    @JsonView(AppUserView.class)
    protected LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(nullable = false)
    @JsonView(AppUserView.class)
    protected Profil profil;
}
