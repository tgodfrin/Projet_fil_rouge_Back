package com.locmns.service;

import com.locmns.dao.EquipmentDao;
import com.locmns.dao.StatusEquipmentDao;
import com.locmns.model.Equipment;
import com.locmns.model.StatusEquipment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StatusEquipmentService {

    private final StatusEquipmentDao statusEquipmentDao;
    private final EquipmentDao equipmentDao;

    // Retourne tout l'historique des statuts techniques d'un équipement (pannes passées et en cours)
    public List<StatusEquipment> findByEquipment(Integer equipmentId) throws EquipmentNotFoundException {
        Equipment equipment = equipmentDao.findById(equipmentId)
                .orElseThrow(EquipmentNotFoundException::new);
        return statusEquipmentDao.findByEquipment(equipment);
    }

    // Crée un nouveau statut technique (signalement d'une panne ou mise en réparation)
    // endStatusDate est null à la création — la panne est en cours
    // beginStatusDate est géré automatiquement par @CreationTimestamp sur l'entité
    public void create(StatusEquipment statusEquipment) {
        statusEquipment.setId(null);
        // On s'assure que endStatusDate est null à la création — la panne est ouverte
        statusEquipment.setEndStatusDate(null);
        statusEquipmentDao.save(statusEquipment);
    }

    // Clôture un statut technique (résolution de panne ou fin de réparation)
    // On ne supprime pas l'entrée — on remplit endStatusDate avec l'heure actuelle
    // Ainsi l'historique est conservé et le statut passe de "actif" à "terminé"
    public void resolve(Integer id) throws StatusEquipmentNotFoundException {
        StatusEquipment statusEquipment = statusEquipmentDao.findById(id)
                .orElseThrow(StatusEquipmentNotFoundException::new);
        // endStatusDate IS NULL = en cours → on le remplit pour clôturer
        statusEquipment.setEndStatusDate(LocalDateTime.now());
        statusEquipmentDao.save(statusEquipment);
    }

    public static class EquipmentNotFoundException extends Exception {}
    public static class StatusEquipmentNotFoundException extends Exception {}
}
