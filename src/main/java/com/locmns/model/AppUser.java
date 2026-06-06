package com.locmns.model;

import com.fasterxml.jackson.annotation.JsonView;
import com.locmns.view.AppUserView;
import com.locmns.view.LoanView;
import jakarta.persistence.*;
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView({AppUserView.class, LoanView.class})
    protected Integer id;

    @Column(nullable = false, unique = true)
    @JsonView(AppUserView.class)
    protected String email;

    @Column(nullable = false)
    @JsonView({AppUserView.class, LoanView.class})
    protected String name;

    @Column(nullable = false)
    @JsonView({AppUserView.class, LoanView.class})
    protected String lastname;

    @Column(nullable = false)
    // Pas de @JsonView : le mot de passe n'est jamais sérialisé.
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
