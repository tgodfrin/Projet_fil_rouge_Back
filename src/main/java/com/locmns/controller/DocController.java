package com.locmns.controller;

import com.fasterxml.jackson.annotation.JsonView;
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

    // Tous les documents liés à un équipement — utilisé par l'onglet "Documents" de equipment-detail
    @IsUser
    @GetMapping("/doc/equipment/{equipmentId}")
    @JsonView(DocView.class)
    public List<Doc> getByEquipment(@PathVariable Integer equipmentId) {
        return docService.findByEquipment(equipmentId);
    }

    // Créer un document et l'associer à un ou plusieurs équipements
    // Le front envoie : title, url, equipmentIds: [1, 2, ...]
    @IsGestionnaire
    @PostMapping("/doc")
    @JsonView(DocView.class)
    public ResponseEntity<Doc> create(@RequestBody @Valid DocRequest dto) {
        Doc saved = docService.create(toEntity(dto));
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    // Supprimer un document par son id
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
        List<Equipment> equipments = dto.getEquipmentIds() != null
                ? dto.getEquipmentIds().stream().map(id -> {
                    Equipment e = new Equipment();
                    e.setId(id);
                    return e;
                  }).collect(Collectors.toList())
                : Collections.emptyList();
        doc.setEquipments(equipments);
        return doc;
    }
}
