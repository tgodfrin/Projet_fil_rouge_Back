package com.locmns.service;

import com.locmns.dao.EquipmentDao;
import com.locmns.dao.EquipmentFamilyDao;
import com.locmns.dao.ProfilDao;
import com.locmns.model.EquipmentFamily;
import com.locmns.model.Profil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipmentFamilyService {

    private final EquipmentFamilyDao equipmentFamilyDao;
    private final ProfilDao          profilDao;
    private final EquipmentDao       equipmentDao;

    /**
     * Définit quels profils peuvent emprunter une famille (table can_loan).
     * La relation est portée par Profil : on ajoute ou on retire la famille dans la liste de chaque profil.
     * Le tout est transactionnel pour rester atomique.
     */
    @Transactional
    public void setAllowedProfils(Integer familyId, List<Integer> profilIds) throws FamilyNotFoundException {
        EquipmentFamily family = equipmentFamilyDao.findById(familyId)
                .orElseThrow(FamilyNotFoundException::new);

        List<Integer> allowedIds = (profilIds != null) ? profilIds : List.of();
        List<Profil> profils = profilDao.findAll();

        for (Profil profil : profils) {
            boolean shouldBeAllowed = allowedIds.contains(profil.getId());
            boolean alreadyAllowed  = profil.getEquipmentFamilies().stream()
                    .anyMatch(f -> f.getId().equals(familyId));

            if (shouldBeAllowed && !alreadyAllowed) {
                profil.getEquipmentFamilies().add(family);
            } else if (!shouldBeAllowed && alreadyAllowed) {
                profil.getEquipmentFamilies().removeIf(f -> f.getId().equals(familyId));
            }
        }
        profilDao.saveAll(profils);
    }

    /**
     * Supprime une famille. La suppression est refusée si elle contient encore du matériel.
     * Sinon on détache d'abord ses liens can_loan (portés par Profil) pour éviter une violation
     * de clé étrangère, puis on supprime la famille.
     */
    @Transactional
    public void deleteFamily(Integer familyId) throws FamilyNotFoundException, FamilyHasEquipmentException {
        EquipmentFamily family = equipmentFamilyDao.findById(familyId)
                .orElseThrow(FamilyNotFoundException::new);

        if (equipmentDao.existsByEquipmentFamily(family)) {
            throw new FamilyHasEquipmentException();
        }

        // On détache la famille de la liste can_loan de chaque profil avant de la supprimer.
        List<Profil> profils = profilDao.findAll();
        boolean changed = false;
        for (Profil profil : profils) {
            if (profil.getEquipmentFamilies().removeIf(f -> f.getId().equals(familyId))) {
                changed = true;
            }
        }
        if (changed) {
            profilDao.saveAll(profils);
        }

        equipmentFamilyDao.deleteById(familyId);
    }

    public static class FamilyNotFoundException extends Exception {}

    // Levée quand on tente de supprimer une famille qui contient encore du matériel.
    public static class FamilyHasEquipmentException extends Exception {}
}
