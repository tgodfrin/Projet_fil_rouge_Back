package com.locmns.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.locmns.dao.EquipmentDao;
import com.locmns.dto.DocRequest;
import com.locmns.model.Doc;
import com.locmns.model.Equipment;
import com.locmns.service.DocService;
import com.locmns.view.DocView;
import jakarta.validation.Valid;
import com.locmns.security.IsGestionnaire;
import com.locmns.security.IsUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class DocController {

    private final DocService docService;
    private final EquipmentDao equipmentDao;

    // Documents liés à un équipement, pour l'onglet Documents de la fiche.
    @IsUser
    @GetMapping("/doc/equipment/{equipmentId}")
    @JsonView(DocView.class)
    public List<Doc> getByEquipment(@PathVariable Integer equipmentId) {
        return docService.findByEquipment(equipmentId);
    }

    // Crée un document et l'associe à un ou plusieurs équipements.
    @IsGestionnaire
    @PostMapping("/doc")
    @JsonView(DocView.class)
    public ResponseEntity<Doc> create(@RequestBody @Valid DocRequest dto) {
        Doc saved = docService.create(toEntity(dto));
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    // Supprime un document par son id.
    @IsGestionnaire
    @DeleteMapping("/doc/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (docService.findById(id).isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        docService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private Doc toEntity(DocRequest dto) {
        Doc doc = new Doc();
        doc.setTitle(dto.getTitle());
        doc.setUrl(dto.getUrl());
        // On utilise getReferenceById pour obtenir des références managées plutôt que des entités détachées.
        List<Equipment> equipments = dto.getEquipmentIds() != null
                ? dto.getEquipmentIds().stream()
                    .map(id -> equipmentDao.getReferenceById(id))
                    .collect(Collectors.toList())
                : Collections.emptyList();
        doc.setEquipments(equipments);
        return doc;
    }
}
