package com.locmns.dao;

import com.locmns.model.Profil;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfilDao extends JpaRepository<Profil, Integer> {
}