package com.locmns.controller;

import com.locmns.dao.AppUserDao;
import com.locmns.dto.ForgotPasswordRequest;
import com.locmns.model.AppUser;
import com.locmns.security.AppUserDetails;
import com.locmns.security.JwtService;
import com.locmns.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService            jwtService;
    private final AppUserDao            appUserDao;
    private final PasswordEncoder       passwordEncoder;
    private final EmailService          emailService;

    // Reçoit l'email et le mot de passe.
    public record LoginRequest(String email, String password) {}

    /**
     * POST /login : reçoit { email, password } et renvoie le token JWT en clair.
     */
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        try {
            AppUserDetails appUserDetails = (AppUserDetails) authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(
                            request.email(), request.password()))
                    .getPrincipal();

            String role = appUserDetails.getUser().getProfil().getType().name();
            String jwt = jwtService.generateToken(request.email(), role);

            return new ResponseEntity<>(jwt, HttpStatus.OK);

        } catch (AuthenticationException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * POST /auth/forgot-password (public). Génère un mot de passe temporaire, l'enregistre haché
     * et l'envoie par email. Répond toujours 200, même si l'email est inconnu, pour ne pas
     * révéler l'existence d'un compte.
     */
    @PostMapping("/auth/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        Optional<AppUser> opt = appUserDao.findByEmail(request.getEmail());
        if (opt.isPresent()) {
            AppUser user = opt.get();
            // Mot de passe temporaire de 8 caractères.
            String temporaryPassword = UUID.randomUUID().toString().substring(0, 8);
            user.setPassword(passwordEncoder.encode(temporaryPassword));
            appUserDao.save(user);
            emailService.sendPasswordEmail(user.getEmail(), user.getName(), temporaryPassword);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
