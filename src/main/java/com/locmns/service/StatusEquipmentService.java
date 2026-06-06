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

    // Historique des statuts techniques d'un équipement (pannes passées et en cours).
    public List<StatusEquipment> findByEquipment(Integer equipmentId) throws EquipmentNotFoundException {
        Equipment equipment = equipmentDao.findById(equipmentId)
                .orElseThrow(EquipmentNotFoundException::new);
        return statusEquipmentDao.findByEquipment(equipment);
    }

    // Crée un statut technique (signalement de panne ou mise en réparation).
    // endStatusDate reste null à la création : la panne est en cours.
    // beginStatusDate est rempli automatiquement par Hibernate.
    public void create(StatusEquipment statusEquipment) {
        statusEquipment.setId(null);
        // On force endStatusDate à null : le statut est ouvert à la création.
        statusEquipment.setEndStatusDate(null);
        statusEquipmentDao.save(statusEquipment);
    }

    // Clôture un statut technique (fin de panne ou de réparation).
    // On ne supprime pas la ligne : on renseigne endStatusDate pour conserver l'historique.
    public void resolve(Integer id) throws StatusEquipmentNotFoundException {
        StatusEquipment statusEquipment = statusEquipmentDao.findById(id)
                .orElseThrow(StatusEquipmentNotFoundException::new);
        // endStatusDate null signifie "en cours" : on le renseigne pour clôturer.
        statusEquipment.setEndStatusDate(LocalDateTime.now());
        statusEquipmentDao.save(statusEquipment);
    }

    public static class EquipmentNotFoundException extends Exception {}
    public static class StatusEquipmentNotFoundException extends Exception {}
}
