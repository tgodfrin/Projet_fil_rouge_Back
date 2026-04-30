package com.locmns.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.locmns.model.Doc;
import com.locmns.service.DocService;
import com.locmns.view.DocView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequiredArgsConstructor
public class DocController {

    private final DocService docService;

    // Tous les documents liés à un équipement — utilisé par l'onglet "Documents" de equipment-detail
    @GetMapping("/doc/equipment/{equipmentId}")
    @JsonView(DocView.class)
    public List<Doc> getByEquipment(@PathVariable Integer equipmentId) {
        return docService.findByEquipment(equipmentId);
    }

    // Créer un document et l'associer à un ou plusieurs équipements
    // Le front envoie : title, url, equipments: [{ id }]
    @PostMapping("/doc")
    @JsonView(DocView.class)
    public ResponseEntity<Doc> create(@RequestBody Doc doc) {
        Doc saved = docService.create(doc);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    // Supprimer un document par son id
    @DeleteMapping("/doc/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (docService.findById(id).isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        docService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
