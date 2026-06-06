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

    // Récupère tous les équipements et calcule leur statut avant de les renvoyer.
    public List<Equipment> findAll() {
        List<Equipment> equipments = equipmentDao.findAll();
        // On injecte le statut calculé dans le champ transient de chaque équipement.
        equipments.forEach(this::setCalculatedStatus);
        return equipments;
    }

    public Optional<Equipment> findById(Integer id) {
        Optional<Equipment> opt = equipmentDao.findById(id);
        opt.ifPresent(this::setCalculatedStatus);
        return opt;
    }

    /**
     * Équipements dont la famille est autorisée par le profil de l'utilisateur.
     * Sert au catalogue côté utilisateur et masque les familles hors de son périmètre.
     * Si le profil n'autorise aucune famille, renvoie une liste vide.
     */
    public List<Equipment> findForCatalogue(Integer userId) {
        AppUser user = appUserDao.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        List<EquipmentFamily> allowedFamilies = user.getProfil().getEquipmentFamilies();
        if (allowedFamilies.isEmpty()) return List.of();

        List<Equipment> results = equipmentDao.findByEquipmentFamilyIn(allowedFamilies);
        results.forEach(this::setCalculatedStatus);
        return results;
    }

    // Équipements disponibles sur une période, filtrés par le profil de l'utilisateur.
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
        // On force id à null pour qu'un client ne puisse pas imposer son propre identifiant.
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

        // On supprime dans un ordre qui respecte les clés étrangères : d'abord les tables de liaison,
        // puis les entités liées à l'équipement et à ses emprunts, et enfin l'équipement lui-même.
        characteristicValueDao.deleteJoinByEquipmentId(id);
        docDao.deleteJoinByEquipmentId(id);
        statusEquipmentDao.deleteByEquipment(equipment);
        eventDao.deleteByEquipmentId(id);   // avant les emprunts, à cause de la clé étrangère event.loan_id
        loanDao.deleteByEquipment(equipment);
        equipmentDao.deleteById(id);
    }

    // Calcule le statut affiché et l'injecte dans le champ transient de l'équipement.
    // Priorité : hors service ou en réparation, puis en prêt, puis disponible.
    private void setCalculatedStatus(Equipment equipment) {

        // Un statut technique encore ouvert (endStatusDate null) est prioritaire sur tout le reste.
        List<StatusEquipment> activeStatuses = statusEquipmentDao
                .findByEquipmentAndEndStatusDateIsNull(equipment);

        if (!activeStatuses.isEmpty()) {
            // Le nom de l'enum donne directement "OUT_OF_SERVICE" ou "UNDER_REPAIR".
            equipment.setStatus(activeStatuses.get(0).getStatusEquipmentType().name());
            return;
        }

        // Sinon, l'équipement est en prêt s'il existe un emprunt validé déjà commencé.
        // Un emprunt validé mais pas encore démarré ne le rend pas indisponible.
        boolean isOnLoan = loanDao.existsByEquipmentAndStatusTypeAndBeginDateLessThanEqual(
                equipment, StatusLoanType.VALID, LocalDate.now());
        equipment.setStatus(isOnLoan ? "EN_PRET" : "DISPONIBLE");
    }

    // Tous les équipements avec leur statut calculé sur la période, pour la vue parc du gestionnaire.
    public List<Equipment> findAllWithStatusForPeriod(LocalDate startDate, LocalDate endDate) {
        List<Equipment> equipments = equipmentDao.findAll();
        equipments.forEach(e -> setCalculatedStatusForPeriod(e, startDate, endDate));
        return equipments;
    }

    // Calcule le statut d'un équipement sur une période (variante de setCalculatedStatus).
    // Priorité : hors service ou en réparation, puis en prêt, puis disponible.
    private void setCalculatedStatusForPeriod(Equipment equipment, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDt = startDate.atStartOfDay();
        LocalDateTime endDt   = endDate.atTime(23, 59, 59);

        // Statut technique actif (panne ou réparation) qui chevauche la période.
        boolean hasTechnicalIssue = statusEquipmentDao
                .existsByEquipmentOverlappingPeriod(equipment, startDt, endDt);

        if (hasTechnicalIssue) {
            // On récupère le type exact du statut actif à renvoyer.
            List<StatusEquipment> active = statusEquipmentDao.findByEquipmentAndEndStatusDateIsNull(equipment);
            equipment.setStatus(active.isEmpty() ? "OUT_OF_SERVICE" : active.get(0).getStatusEquipmentType().name());
            return;
        }

        // Un emprunt validé occupe le matériel tant qu'il n'a pas été rendu.
        // La date de fin prévue ne libère pas le matériel : seul le retour effectif (realEndDate) le fait.
        // Un emprunt validé en retard reste donc "en prêt", y compris sur une période future.
        boolean isOnLoan = loanDao.existsValidLoanOccupyingPeriod(equipment, startDate, endDate);
        equipment.setStatus(isOnLoan ? "EN_PRET" : "DISPONIBLE");
    }

    public static class EquipmentNotFoundException extends Exception {}
}
