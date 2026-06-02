package com.locmns.service;

import com.locmns.dao.AppUserDao;
import com.locmns.dao.CharacteristicValueDao;
import com.locmns.dao.DocDao;
import com.locmns.dao.EquipmentDao;
import com.locmns.dao.EventDao;
import com.locmns.dao.LoanDao;
import com.locmns.dao.StatusEquipmentDao;
import com.locmns.enums.StatusLoanType;
import com.locmns.model.AppUser;
import com.locmns.model.Equipment;
import com.locmns.model.EquipmentFamily;
import com.locmns.model.StatusEquipment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentDao equipmentDao;
    private final LoanDao loanDao;
    private final StatusEquipmentDao statusEquipmentDao;
    private final CharacteristicValueDao characteristicValueDao;
    private final DocDao docDao;
    private final EventDao eventDao;
    private final AppUserDao appUserDao;

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
    // Convertit LocalDate → LocalDateTime pour la query JPQL (qui compare aussi StatusEquipment)
    public List<Equipment> findAvailableForPeriod(LocalDate begin, LocalDate end) {
        LocalDateTime beginDt = begin.atStartOfDay();
        LocalDateTime endDt   = end.atTime(23, 59, 59);
        List<Equipment> available = equipmentDao.findAvailableEquipments(beginDt, endDt);
        available.forEach(e -> e.setStatus("DISPONIBLE"));
        return available;
    }

    // Recherche par nom (insensible à la casse, partiel) + calcul statut
    public List<Equipment> searchByName(String q) {
        List<Equipment> results = equipmentDao.findByEquipmentNameContainingIgnoreCase(q);
        results.forEach(this::setCalculatedStatus);
        return results;
    }

    /**
     * Retourne uniquement les équipements dont la famille est autorisée par le profil de l'utilisateur.
     * Utilisé pour le catalogue côté utilisateur — masque les familles hors périmètre.
     * Si le profil n'a aucune famille configurée, retourne une liste vide.
     */
    public List<Equipment> findForCatalogue(Integer userId) {
        AppUser user = appUserDao.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        List<EquipmentFamily> allowedFamilies = user.getProfil().getEquipmentFamilies();
        if (allowedFamilies.isEmpty()) return List.of();

        List<Equipment> results = equipmentDao.findByEquipmentFamilyIn(allowedFamilies);
        results.forEach(this::setCalculatedStatus);
        return results;
    }

    // Retourne les équipements disponibles sur une période donnée, filtrés par profil utilisateur
    public List<Equipment> findAvailableForCatalogue(Integer userId, LocalDate begin, LocalDate end) {
        AppUser user = appUserDao.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        List<EquipmentFamily> allowedFamilies = user.getProfil().getEquipmentFamilies();
        if (allowedFamilies.isEmpty()) return List.of();

        LocalDateTime beginDt = begin.atStartOfDay();
        LocalDateTime endDt   = end.atTime(23, 59, 59);
        List<Equipment> available = equipmentDao.findAvailableEquipmentsInFamilies(beginDt, endDt, allowedFamilies);
        available.forEach(e -> e.setStatus("DISPONIBLE"));
        return available;
    }

    // Filtre par famille + calcul statut
    public List<Equipment> findByFamily(Integer familyId) {
        EquipmentFamily family = new EquipmentFamily();
        family.setId(familyId);
        List<Equipment> results = equipmentDao.findByEquipmentFamily(family);
        results.forEach(this::setCalculatedStatus);
        return results;
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


    @Transactional
    public void delete(Integer id) throws EquipmentNotFoundException {
        Equipment equipment = equipmentDao.findById(id)
                .orElseThrow(EquipmentNotFoundException::new);

        // Deletion in FK-safe order:
        // 1. ManyToMany join tables
        characteristicValueDao.deleteJoinByEquipmentId(id);
        docDao.deleteJoinByEquipmentId(id);
        // 2. Entities with direct FK to equipment or its loans
        statusEquipmentDao.deleteByEquipment(equipment);
        eventDao.deleteByEquipmentId(id);   // must come before loans (event.loan_id FK)
        loanDao.deleteByEquipment(equipment);
        // 3. The equipment itself
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
