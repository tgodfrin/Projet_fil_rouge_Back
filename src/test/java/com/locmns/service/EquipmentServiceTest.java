package com.locmns.service;

import com.locmns.dao.AppUserDao;
import com.locmns.dao.CharacteristicValueDao;
import com.locmns.dao.DocDao;
import com.locmns.dao.EquipmentDao;
import com.locmns.dao.EventDao;
import com.locmns.dao.LoanDao;
import com.locmns.dao.StatusEquipmentDao;
import com.locmns.enums.StatusEquipmentType;
import com.locmns.enums.StatusLoanType;
import com.locmns.model.Equipment;
import com.locmns.model.StatusEquipment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du calcul de statut d'un équipement.
 * On passe par findById, car setCalculatedStatus est privé : la méthode publique
 * renvoie l'équipement avec son champ status (transient) renseigné.
 * Couvre le plan de tests TU02 (DISPONIBLE) et TU03 (EN_PRET) + les deux statuts techniques.
 */
@ExtendWith(MockitoExtension.class)
class EquipmentServiceTest {

    @Mock private EquipmentDao equipmentDao;
    @Mock private LoanDao loanDao;
    @Mock private StatusEquipmentDao statusEquipmentDao;
    @Mock private CharacteristicValueDao characteristicValueDao;
    @Mock private DocDao docDao;
    @Mock private EventDao eventDao;
    @Mock private AppUserDao appUserDao;

    @InjectMocks private EquipmentService equipmentService;

    private Equipment equipment(int id) {
        Equipment e = new Equipment();
        e.setId(id);
        return e;
    }

    @Test
    void statut_disponible_quand_aucun_statut_actif_ni_emprunt() {
        Equipment e = equipment(1);
        when(equipmentDao.findById(1)).thenReturn(Optional.of(e));
        when(statusEquipmentDao.findByEquipmentAndEndStatusDateIsNull(any(Equipment.class)))
                .thenReturn(List.of());
        when(loanDao.existsByEquipmentAndStatusTypeAndBeginDateLessThanEqual(
                any(Equipment.class), eq(StatusLoanType.VALID), any(LocalDate.class)))
                .thenReturn(false);

        Equipment result = equipmentService.findById(1).orElseThrow();

        assertThat(result.getStatus()).isEqualTo("DISPONIBLE");
    }

    @Test
    void statut_en_pret_quand_emprunt_valide_en_cours() {
        Equipment e = equipment(1);
        when(equipmentDao.findById(1)).thenReturn(Optional.of(e));
        when(statusEquipmentDao.findByEquipmentAndEndStatusDateIsNull(any(Equipment.class)))
                .thenReturn(List.of());
        when(loanDao.existsByEquipmentAndStatusTypeAndBeginDateLessThanEqual(
                any(Equipment.class), eq(StatusLoanType.VALID), any(LocalDate.class)))
                .thenReturn(true);

        Equipment result = equipmentService.findById(1).orElseThrow();

        assertThat(result.getStatus()).isEqualTo("EN_PRET");
    }

    @Test
    void statut_out_of_service_prioritaire_sur_le_reste() {
        Equipment e = equipment(1);
        StatusEquipment panne = new StatusEquipment();
        panne.setStatusEquipmentType(StatusEquipmentType.OUT_OF_SERVICE);
        when(equipmentDao.findById(1)).thenReturn(Optional.of(e));
        when(statusEquipmentDao.findByEquipmentAndEndStatusDateIsNull(any(Equipment.class)))
                .thenReturn(List.of(panne));

        Equipment result = equipmentService.findById(1).orElseThrow();

        assertThat(result.getStatus()).isEqualTo("OUT_OF_SERVICE");
    }

    @Test
    void statut_under_repair_quand_reparation_active() {
        Equipment e = equipment(1);
        StatusEquipment reparation = new StatusEquipment();
        reparation.setStatusEquipmentType(StatusEquipmentType.UNDER_REPAIR);
        when(equipmentDao.findById(1)).thenReturn(Optional.of(e));
        when(statusEquipmentDao.findByEquipmentAndEndStatusDateIsNull(any(Equipment.class)))
                .thenReturn(List.of(reparation));

        Equipment result = equipmentService.findById(1).orElseThrow();

        assertThat(result.getStatus()).isEqualTo("UNDER_REPAIR");
    }

    /**
     * Anti-régression du bug des retards : un emprunt validé non rendu occupe l'équipement
     * même si sa date de fin est dépassée. Le statut sur la période doit rester EN_PRET,
     * jamais DISPONIBLE.
     */
    @Test
    void emprunt_valide_en_retard_reste_en_pret_sur_la_periode() {
        Equipment e = equipment(1);
        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now().plusDays(5);
        when(equipmentDao.findAll()).thenReturn(List.of(e));
        when(statusEquipmentDao.existsByEquipmentOverlappingPeriod(any(Equipment.class), any(), any()))
                .thenReturn(false);
        when(loanDao.existsValidLoanOccupyingPeriod(any(Equipment.class), eq(start), eq(end)))
                .thenReturn(true);

        Equipment result = equipmentService.findAllWithStatusForPeriod(start, end).get(0);

        assertThat(result.getStatus()).isEqualTo("EN_PRET");
    }
}
