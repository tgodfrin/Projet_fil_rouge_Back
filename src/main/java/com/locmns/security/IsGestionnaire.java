package com.locmns.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Acces reserve aux gestionnaires et administrateurs
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAnyRole('ROLE_GESTIONNAIRE', 'ROLE_ADMINISTRATEUR')")
public @interface IsGestionnaire {
}
