package com.locmns.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Accès pour tout utilisateur authentifié.
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAnyAuthority('ROLE_GESTIONNAIRE', 'ROLE_COLLABORATEUR', 'ROLE_INTERVENANT', 'ROLE_STAGIAIRE')")
public @interface IsUser {
}
