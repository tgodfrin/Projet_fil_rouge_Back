package com.locmns.dao;

import com.locmns.enums.ProfilType;
import com.locmns.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AppUserDao extends JpaRepository<AppUser, Integer> {

    // Recherche un utilisateur par son email, par exemple lors de la connexion.
    Optional<AppUser> findByEmail(String email);

    // Tous les utilisateurs d'un type de profil donné.
    List<AppUser> findByProfilType(ProfilType type);

    // Recherche insensible à la casse sur le nom, le prénom ou l'email.
    @Query("SELECT u FROM AppUser u WHERE " +
           "LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.lastname) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<AppUser> searchByNameOrLastnameOrEmail(@Param("search") String search);
}