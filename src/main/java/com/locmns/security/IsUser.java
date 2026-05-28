package com.locmns.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Acces pour tout utilisateur authentifie (tous les roles)
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAnyAuthority('ROLE_GESTIONNAIRE', 'ROLE_COLLABORATEUR', 'ROLE_INTERVENANT', 'ROLE_STAGIAIRE', 'ROLE_ADMINISTRATEUR')")
public @interface IsUser {
}
