package com.locmns.validation;

import com.locmns.TestUtils;
import com.locmns.dto.AppUserRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires de la validation Bean Validation sur le DTO de création d'utilisateur.
 * On invoque directement le Validator, comme le fait Spring lors du @Valid sur le @RequestBody.
 * Couvre TU01 (email mal formé) + le cas champ vide.
 */
class AppUserValidationTest {

    private static Validator validator;

    @BeforeAll
    static void initValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    // DTO complet et valide, qu'on dégrade champ par champ dans chaque test.
    private AppUserRequest validRequest() {
        AppUserRequest dto = new AppUserRequest();
        dto.setEmail("jean.martin@mns.fr");
        dto.setName("Jean");
        dto.setLastname("Martin");
        dto.setPassword("admin123");
        dto.setProfilId(1);
        return dto;
    }

    @Test
    void email_mal_forme_est_rejete() {
        AppUserRequest dto = validRequest();
        dto.setEmail("pasunemail");

        boolean violationExiste = TestUtils.constraintViolationExist(
                validator.validate(dto), "email", "Email");

        assertThat(violationExiste).isTrue();
    }

    @Test
    void email_vide_est_rejete() {
        AppUserRequest dto = validRequest();
        dto.setEmail("");

        boolean violationExiste = TestUtils.constraintViolationExist(
                validator.validate(dto), "email", "NotBlank");

        assertThat(violationExiste).isTrue();
    }

    @Test
    void nom_vide_est_rejete() {
        AppUserRequest dto = validRequest();
        dto.setName("");

        boolean violationExiste = TestUtils.constraintViolationExist(
                validator.validate(dto), "name", "NotBlank");

        assertThat(violationExiste).isTrue();
    }

    @Test
    void dto_complet_ne_leve_aucune_violation() {
        assertThat(validator.validate(validRequest())).isEmpty();
    }
}
