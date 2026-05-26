package com.locmns.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.locmns.dto.AppUserRequest;
import com.locmns.model.AppUser;
import com.locmns.model.Profil;
import com.locmns.service.AppUserService;
import com.locmns.view.AppUserView;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import com.locmns.security.IsGestionnaire;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Validated
public class AppUserController {

    private final AppUserService appUserService;

    // Seuls les gestionnaires et admins peuvent lister tous les utilisateurs
    @IsGestionnaire
    @GetMapping("/user/list")
    @JsonView(AppUserView.class)
    public List<AppUser> getAll() {
        return appUserService.findAll();
    }

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
        appUserService.create(user);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
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

    // Modifier uniquement l'email (tout utilisateur connecte peut modifier le sien)
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

    // Modifier uniquement le password
    @PutMapping("/user/{id}/password")
    public ResponseEntity<Void> updatePassword(
            @PathVariable Integer id,
            @RequestParam @NotBlank(message = "L'ancien mot de passe ne peut pas etre vide") String oldPassword,
            @RequestParam @NotBlank(message = "Le nouveau mot de passe ne peut pas etre vide") String password) {
        try {
            appUserService.updatePassword(id, oldPassword, password);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (AppUserService.UserNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (AppUserService.InvalidPasswordException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
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
