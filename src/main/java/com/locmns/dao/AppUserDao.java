package com.locmns.dao;

import com.locmns.model.AppUser;
import com.locmns.model.Profil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AppUserDao extends JpaRepository<AppUser, Integer> {

    Optional<AppUser> findByEmail(String email);

    List<AppUser> findByProfil(Profil profil);

    @Query("SELECT u FROM AppUser u WHERE " +
           "LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.lastname) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<AppUser> searchByNameOrLastnameOrEmail(@Param("search") String search);
}