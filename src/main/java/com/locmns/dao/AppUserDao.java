package com.locmns.dao;

import com.locmns.model.AppUser;
import com.locmns.model.Profil;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppUserDao extends JpaRepository<AppUser, Integer> {

    Optional<AppUser> findByEmail(String email);

    List<AppUser> findByProfil(Profil profil);
}