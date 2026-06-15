package com.locmns.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.locmns.dto.AppUserRequest;
import com.locmns.dto.AppUserUpdateRequest;
import com.locmns.dto.ChangeEmailRequest;
import com.locmns.dto.ChangePasswordRequest;
import com.locmns.model.AppUser;
import com.locmns.model.Profil;
import com.locmns.service.AppUserService;
import com.locmns.view.AppUserView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Utilisateurs", description = "Gestion des comptes (gestionnaire) et profil de l'utilisateur connecté : email et mot de passe.")
public class AppUserController {

    private final AppUserService appUserService;

    // Seuls les gestionnaires peuvent lister tous les utilisateurs.
    @Operation(summary = "Lister tous les utilisateurs", description = "Gestionnaire uniquement.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des utilisateurs"),
            @ApiResponse(responseCode = "403", description = "Réservé au gestionnaire")
    })
    @IsGestionnaire
    @GetMapping("/user/list")
    @JsonView(AppUserView.class)
    public List<AppUser> getAll() {
        return appUserService.findAll();
    }

    // Utilisateur actuellement connecté, identifié à partir du token JWT.
    @Operation(summary = "Utilisateur connecté", description = "Renvoie le compte identifié par le token JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Compte de l'utilisateur connecté"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "404", description = "Compte introuvable")
    })
    @IsUser
    @GetMapping("/user/me")
    @JsonView(AppUserView.class)
    public ResponseEntity<AppUser> getMe(@AuthenticationPrincipal AppUserDetails userDetails) {
        if (userDetails == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        Optional<AppUser> opt = appUserService.findById(userDetails.getUser().getId());
        if (opt.isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(opt.get(), HttpStatus.OK);
    }

    @Operation(summary = "Détail d'un utilisateur", description = "Gestionnaire uniquement.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Utilisateur trouvé"),
            @ApiResponse(responseCode = "403", description = "Réservé au gestionnaire"),
            @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    })
    @IsGestionnaire
    @GetMapping("/user/{id}")
    @JsonView(AppUserView.class)
    public ResponseEntity<AppUser> getById(@PathVariable Integer id) {
        Optional<AppUser> opt = appUserService.findById(id);
        if (opt.isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(opt.get(), HttpStatus.OK);
    }

    // Seuls les gestionnaires peuvent créer un utilisateur.
    @Operation(summary = "Créer un utilisateur", description = "Le mot de passe est haché en BCrypt avant persistance. Gestionnaire uniquement.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Utilisateur créé"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "403", description = "Réservé au gestionnaire")
    })
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
    @Operation(summary = "Rechercher des utilisateurs", description = "Recherche par nom, prénom ou email (insensible à la casse). Gestionnaire uniquement.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Résultats de la recherche"),
            @ApiResponse(responseCode = "403", description = "Réservé au gestionnaire")
    })
    @IsGestionnaire
    @GetMapping("/user/search")
    @JsonView(AppUserView.class)
    public List<AppUser> search(@RequestParam String q) {
        return appUserService.search(q);
    }

    // Tous les utilisateurs d'un profil donné.
    @Operation(summary = "Utilisateurs par profil", description = "Liste les comptes d'un type de profil (GESTIONNAIRE, COLLABORATEUR, INTERVENANT, STAGIAIRE). Gestionnaire uniquement.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Utilisateurs du profil"),
            @ApiResponse(responseCode = "403", description = "Réservé au gestionnaire")
    })
    @IsGestionnaire
    @GetMapping("/user/profil/{type}")
    @JsonView(AppUserView.class)
    public List<AppUser> getByProfil(@PathVariable String type) {
        return appUserService.findByProfil(type);
    }

    // L'utilisateur connecté change son propre mot de passe.
    // Les identifiants passent par le corps de la requête, jamais dans l'URL.
    @Operation(
            summary = "Changer son propre mot de passe",
            description = "Exige l'ancien mot de passe (vérifié via BCrypt) et le nouveau. Identifiants dans le corps, jamais dans l'URL."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Mot de passe modifié"),
            @ApiResponse(responseCode = "401", description = "Ancien mot de passe incorrect"),
            @ApiResponse(responseCode = "404", description = "Compte introuvable")
    })
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
    @Operation(summary = "Changer son propre email", description = "Le nouvel email est envoyé dans le corps de la requête.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Email modifié"),
            @ApiResponse(responseCode = "400", description = "Email invalide"),
            @ApiResponse(responseCode = "404", description = "Compte introuvable")
    })
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
    @Operation(summary = "Modifier l'email d'un utilisateur", description = "Gestionnaire uniquement.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Email modifié"),
            @ApiResponse(responseCode = "400", description = "Email invalide"),
            @ApiResponse(responseCode = "403", description = "Réservé au gestionnaire"),
            @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    })
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

    // Modification du mot de passe par id : un utilisateur ne peut changer que le sien (y compris un gestionnaire pour son propre compte).
    // La réinitialisation du mot de passe d'un autre compte n'est pas permise ici : elle passe par "mot de passe oublié".
    // Les identifiants passent par le corps de la requête, jamais dans l'URL.
    @Operation(
            summary = "Modifier son propre mot de passe par id",
            description = "Un utilisateur ne peut modifier que son propre mot de passe (l'ancien mot de passe est exigé et vérifié via BCrypt). "
                    + "La réinitialisation du mot de passe d'un autre compte passe par 'mot de passe oublié'. Identifiants dans le corps, jamais dans l'URL."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Mot de passe modifié"),
            @ApiResponse(responseCode = "401", description = "Ancien mot de passe incorrect"),
            @ApiResponse(responseCode = "403", description = "Modification du mot de passe d'un autre compte interdite"),
            @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    })
    @IsUser
    @PutMapping("/user/{id}/password")
    public ResponseEntity<Void> updatePassword(
            @PathVariable Integer id,
            @AuthenticationPrincipal AppUserDetails userDetails,
            @RequestBody @Valid ChangePasswordRequest dto) {
        // On ne peut modifier que son propre mot de passe : un gestionnaire ne change pas celui d'un autre compte.
        if (!userDetails.getUser().getId().equals(id)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        try {
            appUserService.updatePassword(id, dto.getOldPassword(), dto.getPassword());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (AppUserService.UserNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (AppUserService.InvalidPasswordException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    // Met à jour les informations d'un utilisateur, sans le mot de passe (gestionnaire uniquement).
    @Operation(summary = "Modifier un utilisateur", description = "Met à jour nom, prénom, email et profil (sans le mot de passe). Gestionnaire uniquement.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Utilisateur modifié"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "403", description = "Réservé au gestionnaire"),
            @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    })
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
    @Operation(summary = "Supprimer un utilisateur", description = "Renvoie 409 si l'utilisateur a encore des emprunts ou des données liées. Gestionnaire uniquement.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Utilisateur supprimé"),
            @ApiResponse(responseCode = "403", description = "Réservé au gestionnaire"),
            @ApiResponse(responseCode = "404", description = "Utilisateur introuvable"),
            @ApiResponse(responseCode = "409", description = "Utilisateur lié à des emprunts ou données existantes")
    })
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
