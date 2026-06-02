package com.locmns.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.locmns.dto.AppUserRequest;
import com.locmns.dto.AppUserUpdateRequest;
import com.locmns.model.AppUser;
import com.locmns.model.Profil;
import com.locmns.service.AppUserService;
import com.locmns.service.EmailService;
import com.locmns.view.AppUserView;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import com.locmns.security.IsGestionnaire;
import com.locmns.security.IsUser;
import com.locmns.security.AppUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Validated
public class AppUserController {

    private final AppUserService appUserService;
    private final EmailService   emailService;

    // Seuls les gestionnaires et admins peuvent lister tous les utilisateurs
    @IsGestionnaire
    @GetMapping("/user/list")
    @JsonView(AppUserView.class)
    public List<AppUser> getAll() {
        return appUserService.findAll();
    }

    // Retourne l'utilisateur actuellement authentifié (lu depuis le token JWT)
    @IsUser
    @GetMapping("/user/me")
    @JsonView(AppUserView.class)
    public ResponseEntity<AppUser> getMe(@AuthenticationPrincipal AppUserDetails userDetails) {
        if (userDetails == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        Optional<AppUser> opt = appUserService.findById(userDetails.getUser().getId());
        if (opt.isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(opt.get(), HttpStatus.OK);
    }

    @IsGestionnaire
    @GetMapping("/user/{id}")
    @JsonView(AppUserView.class)
    public ResponseEntity<AppUser> getById(@PathVariable Integer id) {
        Optional<AppUser> opt = appUserService.findById(id);
        if (opt.isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(opt.get(), HttpStatus.OK);
    }

    // Seuls les gestionnaires et admins peuvent creer un utilisateur
    @IsGestionnaire
    @PostMapping("/user")
    @JsonView(AppUserView.class)
    public ResponseEntity<AppUser> create(@RequestBody @Validated AppUserRequest dto) {
        AppUser user = toEntity(dto);
        AppUser saved = appUserService.create(user);
        // Reload from DB to get fully-initialized Profil proxy (avoids LazyInitializationException)
        return appUserService.findById(saved.getId())
                .map(u -> new ResponseEntity<>(u, HttpStatus.CREATED))
                .orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    // GET /user/search?q= -> recherche serveur par nom, prenom ou email
    @IsGestionnaire
    @GetMapping("/user/search")
    @JsonView(AppUserView.class)
    public List<AppUser> search(@RequestParam String q) {
        return appUserService.search(q);
    }

    // GET /user/profil/{type} -> tous les utilisateurs d'un profil donne
    @IsGestionnaire
    @GetMapping("/user/profil/{type}")
    @JsonView(AppUserView.class)
    public List<AppUser> getByProfil(@PathVariable String type) {
        return appUserService.findByProfil(type);
    }

    // Allows the authenticated collaborator to change their own password (no gestionnaire role required)
    @IsUser
    @PutMapping("/user/me/password")
    public ResponseEntity<Void> updateMyPassword(
            @AuthenticationPrincipal AppUserDetails userDetails,
            @RequestParam @NotBlank(message = "L'ancien mot de passe ne peut pas etre vide") String oldPassword,
            @RequestParam @NotBlank(message = "Le nouveau mot de passe ne peut pas etre vide") String password) {
        try {
            appUserService.updatePassword(userDetails.getUser().getId(), oldPassword, password);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (AppUserService.UserNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (AppUserService.InvalidPasswordException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    // Modifier son propre email (tous les utilisateurs authentifies)
    @IsUser
    @PutMapping("/user/me/email")
    public ResponseEntity<Void> updateMyEmail(
            @AuthenticationPrincipal AppUserDetails userDetails,
            @RequestParam @NotBlank(message = "L'email ne peut pas etre vide")
            @Email(message = "L'email est mal forme") String email) {
        try {
            appUserService.updateEmail(userDetails.getUser().getId(), email);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (AppUserService.UserNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Modifier uniquement l'email (gestionnaire uniquement — par ID)
    @IsGestionnaire
    @PutMapping("/user/{id}/email")
    public ResponseEntity<Void> updateEmail(
            @PathVariable Integer id,
            @RequestParam @NotBlank(message = "L'email ne peut pas etre vide")
            @Email(message = "L'email est mal forme") String email) {
        try {
            appUserService.updateEmail(id, email);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (AppUserService.UserNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Modifier le password par ID — gestionnaire peut changer n'importe qui,
    // utilisateur peut changer uniquement son propre mot de passe
    @IsUser
    @PutMapping("/user/{id}/password")
    public ResponseEntity<Void> updatePassword(
            @PathVariable Integer id,
            @AuthenticationPrincipal AppUserDetails userDetails,
            @RequestParam @NotBlank(message = "L'ancien mot de passe ne peut pas etre vide") String oldPassword,
            @RequestParam @NotBlank(message = "Le nouveau mot de passe ne peut pas etre vide") String password) {
        // Un utilisateur ne peut modifier que son propre mot de passe
        // Seul un gestionnaire peut modifier le mot de passe d'un autre utilisateur
        boolean isGestionnaire = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_GESTIONNAIRE"));
        if (!isGestionnaire && !userDetails.getUser().getId().equals(id)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        try {
            appUserService.updatePassword(id, oldPassword, password);
            // When a gestionnaire changes another user's password, notify them by email
            if (isGestionnaire && !userDetails.getUser().getId().equals(id)) {
                appUserService.findById(id).ifPresent(targetUser ->
                    emailService.sendPasswordEmail(targetUser.getEmail(), targetUser.getName(), password)
                );
            }
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (AppUserService.UserNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (AppUserService.InvalidPasswordException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    // Met à jour les informations d'un utilisateur (sans mot de passe) — réservé aux gestionnaires
    @IsGestionnaire
    @PutMapping("/user/{id}")
    @JsonView(AppUserView.class)
    public ResponseEntity<AppUser> update(
            @PathVariable Integer id,
            @RequestBody @Validated AppUserUpdateRequest dto) {
        try {
            AppUser updated = appUserService.update(id, dto.getName(), dto.getLastname(), dto.getEmail(), dto.getProfilId());
            return appUserService.findById(updated.getId())
                    .map(u -> new ResponseEntity<>(u, HttpStatus.OK))
                    .orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
        } catch (AppUserService.UserNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Supprime un utilisateur — réservé aux gestionnaires
    // Retourne 409 si l'utilisateur a des emprunts ou des données liées en base
    @IsGestionnaire
    @DeleteMapping("/user/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            appUserService.delete(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (AppUserService.UserNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (AppUserService.UserHasLoansException e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    private AppUser toEntity(AppUserRequest dto) {
        AppUser user = new AppUser();
        user.setEmail(dto.getEmail());
        user.setName(dto.getName());
        user.setLastname(dto.getLastname());
        user.setPassword(dto.getPassword());
        Profil profil = new Profil();
        profil.setId(dto.getProfilId());
        user.setProfil(profil);
        return user;
    }
}
