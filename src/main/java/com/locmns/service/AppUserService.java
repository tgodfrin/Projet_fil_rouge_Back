package com.locmns.service;

import com.locmns.dao.AppUserDao;
import com.locmns.enums.ProfilType;
import com.locmns.model.AppUser;
import com.locmns.model.Profil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AppUserService {

    public static class UserNotFoundException extends Exception {}

    private final AppUserDao appUserDao;

    public List<AppUser> findAll() {
        return appUserDao.findAll();
    }

    public Optional<AppUser> findById(Integer id) {
        return appUserDao.findById(id);
    }

    public void create(AppUser user) {
        user.setId(null);
        // TODO: user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        appUserDao.save(user);
    }

    // Recherche serveur par nom, prénom ou email (insensible à la casse, contenu partiel)
    public List<AppUser> search(String q) {
        return appUserDao.searchByNameOrLastnameOrEmail(q);
    }

    // Retourne tous les utilisateurs d'un profil donné (GESTIONNAIRE, COLLABORATEUR...)
    public List<AppUser> findByProfil(String profilType) {
        Profil profil = new Profil();
        profil.setType(ProfilType.valueOf(profilType));
        return appUserDao.findByProfil(profil);
    }

    public void updateEmail(Integer id, String newEmail) throws UserNotFoundException {
        AppUser existing = appUserDao.findById(id)
                .orElseThrow(UserNotFoundException::new);
        existing.setEmail(newEmail);
        appUserDao.save(existing);
    }

    public void updatePassword(Integer id, String newPassword) throws UserNotFoundException {
        AppUser existing = appUserDao.findById(id)
                .orElseThrow(UserNotFoundException::new);
        // TODO: existing.setPassword(bCryptPasswordEncoder.encode(newPassword));
        existing.setPassword(newPassword);
        appUserDao.save(existing);
    }
}
