package com.locmns;

import jakarta.validation.ConstraintViolation;

import java.util.Set;

/**
 * Utilitaire de test inspiré du projet de référence : vérifie qu'une contrainte de
 * validation précise (ex. @NotBlank sur "email") fait bien partie des violations relevées.
 */
public class TestUtils {

    public static boolean constraintViolationExist(
            Set<? extends ConstraintViolation<?>> violations,
            String fieldName,
            String annotationName
    ) {
        return violations.stream().anyMatch(violation -> {
            String field = violation.getPropertyPath().toString();
            String annotation = violation
                    .getConstraintDescriptor()
                    .getAnnotation()
                    .annotationType()
                    .getSimpleName();
            return field.equals(fieldName) && annotation.equals(annotationName);
        });
    }
}
