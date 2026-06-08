package com.locmns.service;

import com.locmns.dao.AppUserDao;
import com.locmns.dao.EventDao;
import com.locmns.dao.LoanDao;
import com.locmns.dao.ProfilDao;
import com.locmns.model.AppUser;
import com.locmns.model.Profil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires de la gestion des utilisateurs.
 * Couvre TU05 (changement de mot de passe avec ancien mot de passe erroné)
 * et le hachage BCrypt à la création.
 */
@ExtendWith(MockitoExtension.class)
class AppUserServiceTest {

    @Mock private AppUserDao appUserDao;
    @Mock private ProfilDao profilDao;
    @Mock private LoanDao loanDao;
    @Mock private EventDao eventDao;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private AppUserService appUserService;

    @Test
    void create_hache_le_mot_de_passe_avant_enregistrement() {
        AppUser input = new AppUser();
        input.setPassword("admin123");
        Profil profil = new Profil();
        profil.setId(1);
        input.setProfil(profil);

        when(passwordEncoder.encode("admin123")).thenReturn("HASH_BCRYPT");
        when(profilDao.getReferenceById(1)).thenReturn(profil);
        when(appUserDao.save(any(AppUser.class))).thenAnswer(call -> call.getArgument(0));

        AppUser saved = appUserService.create(input);

        assertThat(saved.getPassword()).isEqualTo("HASH_BCRYPT");
        assertThat(saved.getPassword()).isNotEqualTo("admin123");
        verify(passwordEncoder).encode("admin123");
    }

    @Test
    void updatePassword_remplace_le_mot_de_passe_si_ancien_correct() throws Exception {
        AppUser existing = new AppUser();
        existing.setPassword("HASH_ANCIEN");
        when(appUserDao.findById(1)).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("admin123", "HASH_ANCIEN")).thenReturn(true);
        when(passwordEncoder.encode("nouveau456")).thenReturn("HASH_NOUVEAU");

        appUserService.updatePassword(1, "admin123", "nouveau456");

        assertThat(existing.getPassword()).isEqualTo("HASH_NOUVEAU");
        verify(appUserDao).save(existing);
    }

    @Test
    void updatePassword_leve_une_exception_si_ancien_incorrect() {
        AppUser existing = new AppUser();
        existing.setPassword("HASH_ANCIEN");
        when(appUserDao.findById(1)).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("mauvais", "HASH_ANCIEN")).thenReturn(false);

        assertThatThrownBy(() -> appUserService.updatePassword(1, "mauvais", "nouveau456"))
                .isInstanceOf(AppUserService.InvalidPasswordException.class);
        verify(appUserDao, never()).save(any(AppUser.class));
    }
}
