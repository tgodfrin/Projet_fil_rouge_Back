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

        // 2. Vérifie s'il existe un emprunt VALID dont beginDate <= aujourd'hui
        // Un emprunt validé mais pas encore démarré ne doit pas bloquer l'affichage DISPONIBLE
        boolean isOnLoan = loanDao.existsByEquipmentAndStatusTypeAndBeginDateLessThanEqual(
                equipment, StatusLoanType.VALID, LocalDate.now());
        equipment.setStatus(isOnLoan ? "EN_PRET" : "DISPONIBLE");
    }

    // Retourne tous les équipements avec leur statut calculé sur la période donnée
    // Utilisé par le gestionnaire pour visualiser la disponibilité du parc sur une date ou une plage
    public List<Equipment> findAllWithStatusForPeriod(LocalDate startDate, LocalDate endDate) {
        List<Equipment> equipments = equipmentDao.findAll();
        equipments.forEach(e -> setCalculatedStatusForPeriod(e, startDate, endDate));
        return equipments;
    }

    // Calcule le statut d'un équipement sur une période donnée (variante de setCalculatedStatus)
    // Ordre de priorité : OUT_OF_SERVICE / UNDER_REPAIR > EN_PRET > DISPONIBLE
    private void setCalculatedStatusForPeriod(Equipment equipment, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDt = startDate.atStartOfDay();
        LocalDateTime endDt   = endDate.atTime(23, 59, 59);

        // 1. Statut technique actif sur la période (panne ou réparation qui chevauche)
        boolean hasTechnicalIssue = statusEquipmentDao
                .existsByEquipmentOverlappingPeriod(equipment, startDt, endDt);

        if (hasTechnicalIssue) {
            // On récupère le type exact du statut actif pour le retourner (OUT_OF_SERVICE ou UNDER_REPAIR)
            List<StatusEquipment> active = statusEquipmentDao.findByEquipmentAndEndStatusDateIsNull(equipment);
            equipment.setStatus(active.isEmpty() ? "OUT_OF_SERVICE" : active.get(0).getStatusEquipmentType().name());
            return;
        }

        // 2. Emprunt non-INVALID qui chevauche la période (bornes incluses)
        boolean isOnLoan = loanDao.existsByEquipmentAndStatusTypeNotAndBeginDateLessThanEqualAndEndDateGreaterThanEqual(
                equipment, StatusLoanType.INVALID, endDate, startDate);
        equipment.setStatus(isOnLoan ? "EN_PRET" : "DISPONIBLE");
    }

    public static class EquipmentNotFoundException extends Exception {}
}
