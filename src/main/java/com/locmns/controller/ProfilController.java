package com.locmns.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.locmns.dao.ProfilDao;
import com.locmns.model.Profil;
import com.locmns.view.AppUserView;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin
@RequiredArgsConstructor
public class ProfilController {

    private final ProfilDao profilDao;

    // Utilisé par le formulaire de création d'utilisateur
    @GetMapping("/profil/list")
    @JsonView(AppUserView.class)
    public List<Profil> getAll() {
        return profilDao.findAll();
    }
}
