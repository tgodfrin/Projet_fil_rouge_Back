package com.locmns.service;

import com.locmns.dao.DocDao;
import com.locmns.model.Doc;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DocService {

    private final DocDao docDao;

    // Retourne tous les documents liés à un équipement — utilisé par equipment-detail
    public List<Doc> findByEquipment(Integer equipmentId) {
        return docDao.findByEquipmentsId(equipmentId);
    }

    public Optional<Doc> findById(Integer id) {
        return docDao.findById(id);
    }

    // Crée un doc et l'associe aux équipements envoyés par le front (equipments: [{id}])
    // Hibernate gère l'insertion dans la table fait_reference via la relation @ManyToMany
    public Doc create(Doc doc) {
        return docDao.save(doc);
    }

    // Supprime un doc et ses entrées dans fait_reference (cascade géré par JPA)
    public void delete(Integer id) {
        docDao.deleteById(id);
    }
}
