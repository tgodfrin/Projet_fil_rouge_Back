package com.locmns.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.locmns.model.AppUser;
import com.locmns.service.AppUserService;
import com.locmns.view.AppUserView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin
@RequiredArgsConstructor
public class AppUserController {

    private final AppUserService appUserService;

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

    @PostMapping("/user")
    @JsonView(AppUserView.class)
    public ResponseEntity<AppUser> create(
            @RequestBody @Validated(AppUser.OnCreate.class) AppUser user) {
        appUserService.create(user);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    // Modifier uniquement l'email
    @PutMapping("/user/{id}/email")
    public ResponseEntity<Void> updateEmail(
            @PathVariable Integer id,
            @RequestParam String email) {
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
            @RequestParam String password) {
        try {
            appUserService.updatePassword(id, password);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (AppUserService.UserNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
