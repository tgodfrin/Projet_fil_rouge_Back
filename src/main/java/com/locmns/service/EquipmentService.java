package com.locmns.service;

import com.locmns.dao.EquipmentDao;
import com.locmns.dao.LoanDao;
import com.locmns.dao.StatusEquipmentDao;
import com.locmns.enums.StatusLoanType;
import com.locmns.model.Equipment;
import com.locmns.model.StatusEquipment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentDao equipmentDao;
    private final LoanDao loanDao;
    private final StatusEquipmentDao statusEquipmentDao;

    // Récupère tous les équipements et calcule leur statut avant de les retourner
    public List<Equipment> findAll() {
        List<Equipment> equipments = equipmentDao.findAll();
        // Pour chaque équipement, on injecte le statut calculé dans le champ @Transient
        equipments.forEach(this::setCalculatedStatus);
        return equipments;
    }

    public Optional<Equipment> findById(Integer id) {
        Optional<Equipment> opt = equipmentDao.findById(id);
        opt.ifPresent(this::setCalculatedStatus);
        return opt;
    }

    // Retourne les équipements disponibles sur une période donnée
    // Utilise la query JPQL de EquipmentDao qui filtre en BDD
    public List<Equipment> findAvailableForPeriod(LocalDateTime begin, LocalDateTime end) {
        List<Equipment> available = equipmentDao.findAvailableEquipments(begin, end);
        available.forEach(e -> e.setStatus("DISPONIBLE"));
        return available;
    }

    public void create(Equipment equipment) {
        // on force id=null pour éviter qu'un client impose son propre id
        equipment.setId(null);
        equipmentDao.save(equipment);
    }

    public void update(Integer id, Equipment equipment) throws EquipmentNotFoundException {
        if (!equipmentDao.existsById(id)) {
            throw new EquipmentNotFoundException();
        }
        equipment.setId(id);
        equipmentDao.save(equipment);
    }


    public void delete(Integer id) throws EquipmentNotFoundException {
        if (!equipmentDao.existsById(id)) {
            throw new EquipmentNotFoundException();
        }
        equipmentDao.deleteById(id);
    }

    // Calcule et injecte le statut dans le champ @Transient de l'entité
    // Ordre de priorité : OUT_OF_SERVICE / UNDER_REPAIR > EN_PRET > DISPONIBLE
    private void setCalculatedStatus(Equipment equipment) {

        // 1. Vérifie s'il existe un statut technique actif (panne ou réparation en cours)
        // endStatusDate IS NULL = pas encore clôturé = toujours actif
        List<StatusEquipment> activeStatuses = statusEquipmentDao
                .findByEquipmentAndEndStatusDateIsNull(equipment);

        if (!activeStatuses.isEmpty()) {
            // .name() retourne le nom de l'enum sous forme de String : "OUT_OF_SERVICE" ou "UNDER_REPAIR"
            equipment.setStatus(activeStatuses.get(0).getStatusEquipmentType().name());
            return; // On s'arrête ici — statut technique prioritaire sur tout
        }

        // 2. Vérifie s'il existe un emprunt VALID sur cet équipement
        // VALID = gestionnaire a approuvé, équipement physiquement sorti — donc EN_PRET
        boolean isOnLoan = loanDao.existsByEquipmentAndStatusType(equipment, StatusLoanType.VALID);
        equipment.setStatus(isOnLoan ? "EN_PRET" : "DISPONIBLE");
    }

    public static class EquipmentNotFoundException extends Exception {}
}
