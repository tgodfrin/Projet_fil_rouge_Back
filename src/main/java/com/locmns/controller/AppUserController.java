package com.locmns.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.locmns.dto.AppUserRequest;
import com.locmns.dto.AppUserUpdateRequest;
import com.locmns.dto.ChangeEmailRequest;
import com.locmns.dto.ChangePasswordRequest;
import com.locmns.model.AppUser;
import com.locmns.model.Profil;
import com.locmns.service.AppUserService;
import com.locmns.service.EmailService;
import com.locmns.view.AppUserView;
import jakarta.validation.Valid;
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

    // Seuls les gestionnaires peuvent lister tous les utilisateurs.
    @IsGestionnaire
    @GetMapping("/user/list")
    @JsonView(AppUserView.class)
    public List<AppUser> getAll() {
        return appUserService.findAll();
    }

    // Utilisateur actuellement connecté, identifié à partir du token JWT.
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

    // Seuls les gestionnaires peuvent créer un utilisateur.
    @IsGestionnaire
    @PostMapping("/user")
    @JsonView(AppUserView.class)
    public ResponseEntity<AppUser> create(@RequestBody @Validated AppUserRequest dto) {
        AppUser user = toEntity(dto);
        AppUser saved = appUserService.create(user);
        // On relit depuis la base pour récupérer un profil complet et éviter une LazyInitializationException.
        return appUserService.findById(saved.getId())
                .map(u -> new ResponseEntity<>(u, HttpStatus.CREATED))
                .orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    // Recherche par nom, prénom ou email.
    @IsGestionnaire
    @GetMapping("/user/search")
    @JsonView(AppUserView.class)
    public List<AppUser> search(@RequestParam String q) {
        return appUserService.search(q);
    }

    // Tous les utilisateurs d'un profil donné.
    @IsGestionnaire
    @GetMapping("/user/profil/{type}")
    @JsonView(AppUserView.class)
    public List<AppUser> getByProfil(@PathVariable String type) {
        return appUserService.findByProfil(type);
    }

    // L'utilisateur connecté change son propre mot de passe.
    // Les identifiants passent par le corps de la requête, jamais dans l'URL.
    @IsUser
    @PutMapping("/user/me/password")
    public ResponseEntity<Void> updateMyPassword(
            @AuthenticationPrincipal AppUserDetails userDetails,
            @RequestBody @Valid ChangePasswordRequest dto) {
        try {
            appUserService.updatePassword(userDetails.getUser().getId(), dto.getOldPassword(), dto.getPassword());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (AppUserService.UserNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (AppUserService.InvalidPasswordException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    // L'utilisateur connecté change son propre email (envoyé dans le corps de la requête).
    @IsUser
    @PutMapping("/user/me/email")
    public ResponseEntity<Void> updateMyEmail(
            @AuthenticationPrincipal AppUserDetails userDetails,
            @RequestBody @Valid ChangeEmailRequest dto) {
        try {
            appUserService.updateEmail(userDetails.getUser().getId(), dto.getEmail());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (AppUserService.UserNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Modification de l'email d'un utilisateur par son id (gestionnaire uniquement).
    @IsGestionnaire
    @PutMapping("/user/{id}/email")
    public ResponseEntity<Void> updateEmail(
            @PathVariable Integer id,
            @RequestBody @Valid ChangeEmailRequest dto) {
        try {
            appUserService.updateEmail(id, dto.getEmail());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (AppUserService.UserNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Modification du mot de passe par id : un gestionnaire peut changer celui de n'importe qui,
    // un utilisateur seulement le sien. Les identifiants passent par le corps de la requête, jamais dans l'URL.
    @IsUser
    @PutMapping("/user/{id}/password")
    public ResponseEntity<Void> updatePassword(
            @PathVariable Integer id,
            @AuthenticationPrincipal AppUserDetails userDetails,
            @RequestBody @Valid ChangePasswordRequest dto) {
        // Un utilisateur ne peut modifier que son propre mot de passe ; seul un gestionnaire peut modifier celui d'un autre.
        boolean isGestionnaire = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_GESTIONNAIRE"));
        if (!isGestionnaire && !userDetails.getUser().getId().equals(id)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        try {
            appUserService.updatePassword(id, dto.getOldPassword(), dto.getPassword());
            // Quand un gestionnaire change le mot de passe d'un autre utilisateur, on le prévient par email.
            if (isGestionnaire && !userDetails.getUser().getId().equals(id)) {
                appUserService.findById(id).ifPresent(targetUser ->
                    emailService.sendPasswordEmail(targetUser.getEmail(), targetUser.getName(), dto.getPassword())
                );
            }
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (AppUserService.UserNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (AppUserService.InvalidPasswordException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    // Met à jour les informations d'un utilisateur, sans le mot de passe (gestionnaire uniquement).
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

    // Supprime un utilisateur (gestionnaire uniquement).
    // Renvoie 409 si l'utilisateur a encore des emprunts ou des données liées.
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
