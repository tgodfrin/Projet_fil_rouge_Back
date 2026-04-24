package com.locmns.dao;

import com.locmns.model.AppUser;
import com.locmns.model.Profil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AppUserDao extends JpaRepository<AppUser, Integer> {

    // Recherche un utilisateur par son email (ex: lors du login)
    // Retourne un Optional : résultat potentiellement vide sans lever d'exception
    Optional<AppUser> findByEmail(String email);

    // Retourne tous les utilisateurs ayant un profil donné
    // Ex: findByProfil(GESTIONNAIRE) → liste de tous les gestionnaires
    List<AppUser> findByProfil(Profil profil);

    // Recherche textuelle insensible à la casse sur le nom, prénom ou email
    // @Query nécessaire car la condition est trop complexe pour être déduite du nom de méthode
    // LOWER + LIKE '%...%' : trouve "marc", "Marc", "MARC" indifféremment
    @Query("SELECT u FROM AppUser u WHERE " +
           "LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.lastname) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<AppUser> searchByNameOrLastnameOrEmail(@Param("search") String search);
}