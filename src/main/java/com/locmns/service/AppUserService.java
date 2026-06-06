package com.locmns.service;

import com.locmns.dao.AppUserDao;
import com.locmns.dao.EventDao;
import com.locmns.dao.LoanDao;
import com.locmns.dao.ProfilDao;
import com.locmns.enums.ProfilType;
import com.locmns.enums.StatusLoanType;
import com.locmns.model.AppUser;
import com.locmns.model.Loan;
import com.locmns.model.Profil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AppUserService {

    public static class UserNotFoundException extends Exception {}
    public static class InvalidPasswordException extends Exception {}
    public static class UserHasLoansException extends Exception {}

    private final AppUserDao      appUserDao;
    private final ProfilDao       profilDao;
    private final LoanDao         loanDao;
    private final EventDao        eventDao;
    private final PasswordEncoder passwordEncoder;

    public List<AppUser> findAll() {
        return appUserDao.findAll();
    }

    public Optional<AppUser> findById(Integer id) {
        return appUserDao.findById(id);
    }

    public AppUser create(AppUser user) {
        user.setId(null);
        // Hachage du mot de passe avant l'enregistrement.
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // On charge un profil managé pour éviter l'erreur JPA d'entité détachée.
        Profil managedProfil = profilDao.getReferenceById(user.getProfil().getId());
        user.setProfil(managedProfil);
        return appUserDao.save(user);
    }

    // Recherche par nom, prénom ou email, insensible à la casse et sur contenu partiel.
    public List<AppUser> search(String q) {
        return appUserDao.searchByNameOrLastnameOrEmail(q);
    }

    // Tous les utilisateurs d'un profil donné.
    public List<AppUser> findByProfil(String profilType) {
        return appUserDao.findByProfilType(ProfilType.valueOf(profilType));
    }

    // Met à jour les informations d'un utilisateur, sans le mot de passe.
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

    /**
     * Supprime un utilisateur.
     * Si l'utilisateur a au moins un emprunt validé (matériel encore sorti), la suppression est refusée.
     * Sinon, ses emprunts (en attente, terminés, refusés) et leurs événements sont supprimés avant lui.
     */
    @Transactional
    public void delete(Integer id) throws UserNotFoundException, UserHasLoansException {
        AppUser user = appUserDao.findById(id).orElseThrow(UserNotFoundException::new);

        // On bloque la suppression si l'utilisateur a un emprunt encore en cours (matériel sorti).
        if (loanDao.existsByRequesterAndStatusType(user, StatusLoanType.VALID)) {
            throw new UserHasLoansException();
        }

        // On supprime d'abord les événements, puis tous les emprunts, pour respecter les clés étrangères.
        List<Loan> allLoans = loanDao.findByRequester(user);
        if (!allLoans.isEmpty()) {
            eventDao.deleteByLoanIn(allLoans);
            loanDao.deleteAll(allLoans);
        }

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
        // On vérifie l'ancien mot de passe avant de le remplacer.
        if (!passwordEncoder.matches(oldPassword, existing.getPassword())) {
            throw new InvalidPasswordException();
        }
        // Hachage du nouveau mot de passe avant l'enregistrement.
        existing.setPassword(passwordEncoder.encode(newPassword));
        appUserDao.save(existing);
    }
}
