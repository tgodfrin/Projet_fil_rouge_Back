package com.locmns.controller;

import com.locmns.dao.AppUserDao;
import com.locmns.dto.ForgotPasswordRequest;
import com.locmns.model.AppUser;
import com.locmns.security.AppUserDetails;
import com.locmns.security.JwtService;
import com.locmns.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Authentification", description = "Connexion (JWT) et réinitialisation de mot de passe. Endpoints publics.")
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
    @Operation(
            summary = "Authentifier un utilisateur",
            description = "Vérifie l'email et le mot de passe (BCrypt) puis renvoie un token JWT en clair "
                    + "à placer dans l'en-tête Authorization: Bearer <token> des requêtes suivantes."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authentification réussie : token JWT renvoyé dans le corps"),
            @ApiResponse(responseCode = "401", description = "Email ou mot de passe incorrect")
    })
    @SecurityRequirements // endpoint public : pas de cadenas dans Swagger UI
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
    @Operation(
            summary = "Demander un mot de passe temporaire",
            description = "Génère un mot de passe temporaire, l'enregistre haché et l'envoie par email. "
                    + "Répond toujours 200, même si l'email est inconnu, pour ne pas révéler l'existence d'un compte."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Demande prise en compte (réponse identique que l'email existe ou non)")
    })
    @SecurityRequirements // endpoint public : pas de cadenas dans Swagger UI
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
