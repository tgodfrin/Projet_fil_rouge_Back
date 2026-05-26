package com.locmns.controller;

import com.locmns.security.AppUserDetails;
import com.locmns.security.JwtService;
import com.locmns.model.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService            jwtService;

    // DTO pour recevoir email + password
    public record LoginRequest(String email, String password) {}

    /**
     * POST /login
     * Body JSON : { "email": "...", "password": "..." }
     * Retourne le token JWT en clair (String)
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
}
