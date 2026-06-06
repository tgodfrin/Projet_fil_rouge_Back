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

    // Documents liés à un équipement, pour sa fiche détaillée.
    public List<Doc> findByEquipment(Integer equipmentId) {
        return docDao.findByEquipmentsId(equipmentId);
    }

    public Optional<Doc> findById(Integer id) {
        return docDao.findById(id);
    }

    // Crée un document et l'associe aux équipements envoyés.
    // Hibernate insère les liens dans la table fait_reference via la relation ManyToMany.
    public Doc create(Doc doc) {
        return docDao.save(doc);
    }

    // Supprime un document et ses liens dans fait_reference.
    public void delete(Integer id) {
        docDao.deleteById(id);
    }
}
