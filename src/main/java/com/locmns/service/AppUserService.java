package com.locmns.service;

import com.locmns.dao.AppUserDao;
import com.locmns.dao.ProfilDao;
import com.locmns.enums.ProfilType;
import com.locmns.model.AppUser;
import com.locmns.model.Profil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AppUserService {

    public static class UserNotFoundException extends Exception {}
    public static class InvalidPasswordException extends Exception {}

    private final AppUserDao      appUserDao;
    private final ProfilDao       profilDao;
    private final PasswordEncoder passwordEncoder;

    public List<AppUser> findAll() {
        return appUserDao.findAll();
    }

    public Optional<AppUser> findById(Integer id) {
        return appUserDao.findById(id);
    }

    public AppUser create(AppUser user) {
        user.setId(null);
        // Hachage du mot de passe avant persistance en BDD
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // Charger le Profil manage pour eviter l'erreur "detached entity" de JPA
        Profil managedProfil = profilDao.getReferenceById(user.getProfil().getId());
        user.setProfil(managedProfil);
        return appUserDao.save(user);
    }

    // Recherche serveur par nom, prenom ou email (insensible a la casse, contenu partiel)
    public List<AppUser> search(String q) {
        return appUserDao.searchByNameOrLastnameOrEmail(q);
    }

    // Retourne tous les utilisateurs d'un profil donne (GESTIONNAIRE, COLLABORATEUR...)
    public List<AppUser> findByProfil(String profilType) {
        return appUserDao.findByProfilType(ProfilType.valueOf(profilType));
    }

    // Met à jour les informations d'un utilisateur (sans mot de passe) — réservé aux gestionnaires
    public AppUser update(Integer id, String name, String lastname, String email, Integer profilId)
            throws UserNotFoundException {
        AppUser existing = appUserDao.findById(id).orElseThrow(UserNotFoundException::new);
        existing.setName(name);
        existing.setLastname(lastname);
        existing.setEmail(email);
        Profil managedProfil = profilDao.getReferenceById(profilId);
        existing.setProfil(managedProfil);
        return appUserDao.save(existing);
    }

    // Supprime un utilisateur par son id — réservé aux gestionnaires via le controller
    public void delete(Integer id) throws UserNotFoundException {
        if (!appUserDao.existsById(id)) throw new UserNotFoundException();
        appUserDao.deleteById(id);
    }

    public void updateEmail(Integer id, String newEmail) throws UserNotFoundException {
        AppUser existing = appUserDao.findById(id)
                .orElseThrow(UserNotFoundException::new);
        existing.setEmail(newEmail);
        appUserDao.save(existing);
    }

    public void updatePassword(Integer id, String oldPassword, String newPassword)
            throws UserNotFoundException, InvalidPasswordException {
        AppUser existing = appUserDao.findById(id)
                .orElseThrow(UserNotFoundException::new);
        // Verification de l'ancien mot de passe via BCrypt (compare hash BDD avec plaintext)
        if (!passwordEncoder.matches(oldPassword, existing.getPassword())) {
            throw new InvalidPasswordException();
        }
        // Hachage du nouveau mot de passe avant persistance
        existing.setPassword(passwordEncoder.encode(newPassword));
        appUserDao.save(existing);
    }
}
