package com.locmns.service;

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

    /**
     * Sets which profils are allowed to borrow the given family (can_loan join table).
     * The relationship is owned by Profil, so we add/remove the family on each profil's list.
     * Transactional so the whole update is atomic.
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

    public static class FamilyNotFoundException extends Exception {}
}
