package com.locmns.service;

import com.locmns.dao.AppUserDao;
import com.locmns.dao.EquipmentDao;
import com.locmns.dao.LoanDao;
import com.locmns.enums.StatusLoanType;
import com.locmns.model.AppUser;
import com.locmns.model.Equipment;
import com.locmns.model.EquipmentFamily;
import com.locmns.model.Loan;
import com.locmns.model.Profil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du cycle de vie d'un emprunt.
 * Les DAO sont mockés : on vérifie uniquement la logique métier de LoanService.
 * Couvre TU04 (création nominale) et les transitions de statut + les règles de refus.
 */
@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock private LoanDao loanDao;
    @Mock private AppUserDao appUserDao;
    @Mock private EquipmentDao equipmentDao;

    @InjectMocks private LoanService loanService;

    // Construit un emprunt "brut" tel qu'il arrive du controller (ids seulement).
    private Loan incomingLoan(int requesterId, int equipmentId) {
        Loan loan = new Loan();
        AppUser requester = new AppUser();
        requester.setId(requesterId);
        Equipment equipment = new Equipment();
        equipment.setId(equipmentId);
        loan.setRequester(requester);
        loan.setEquipment(equipment);
        loan.setBeginDate(LocalDate.now());
        loan.setEndDate(LocalDate.now().plusDays(7));
        return loan;
    }

    // Demandeur managé dont le profil autorise la famille donnée.
    private AppUser requesterAllowedFor(EquipmentFamily family) {
        Profil profil = new Profil();
        profil.setEquipmentFamilies(List.of(family));
        AppUser user = new AppUser();
        user.setId(1);
        user.setProfil(profil);
        return user;
    }

    private EquipmentFamily family(int id) {
        EquipmentFamily f = new EquipmentFamily();
        f.setId(id);
        return f;
    }

    private Equipment managedEquipment(int id, EquipmentFamily family) {
        Equipment e = new Equipment();
        e.setId(id);
        e.setEquipmentFamily(family);
        return e;
    }

    @Test
    void create_nominal_passe_le_statut_a_in_progress() throws Exception {
        EquipmentFamily fam = family(10);
        Loan loan = incomingLoan(1, 5);
        when(appUserDao.findById(1)).thenReturn(Optional.of(requesterAllowedFor(fam)));
        when(equipmentDao.findById(5)).thenReturn(Optional.of(managedEquipment(5, fam)));
        when(loanDao.existsByEquipmentAndStatusTypeNotAndBeginDateLessThanAndEndDateGreaterThan(
                any(Equipment.class), eq(StatusLoanType.INVALID), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(false);

        loanService.create(loan);

        assertThat(loan.getStatusType()).isEqualTo(StatusLoanType.IN_PROGRESS);
        assertThat(loan.getValidator()).isNull();
        assertThat(loan.getRealEndDate()).isNull();
        verify(loanDao).save(loan);
    }

    @Test
    void create_refuse_si_famille_non_autorisee() {
        EquipmentFamily allowed = family(10);
        EquipmentFamily other = family(99);
        Loan loan = incomingLoan(1, 5);
        when(appUserDao.findById(1)).thenReturn(Optional.of(requesterAllowedFor(allowed)));
        when(equipmentDao.findById(5)).thenReturn(Optional.of(managedEquipment(5, other)));

        assertThatThrownBy(() -> loanService.create(loan))
                .isInstanceOf(LoanService.UnauthorizedEquipmentFamilyException.class);
        verify(loanDao, never()).save(any(Loan.class));
    }

    @Test
    void create_refuse_si_equipement_deja_reserve_sur_la_periode() {
        EquipmentFamily fam = family(10);
        Loan loan = incomingLoan(1, 5);
        when(appUserDao.findById(1)).thenReturn(Optional.of(requesterAllowedFor(fam)));
        when(equipmentDao.findById(5)).thenReturn(Optional.of(managedEquipment(5, fam)));
        when(loanDao.existsByEquipmentAndStatusTypeNotAndBeginDateLessThanAndEndDateGreaterThan(
                any(Equipment.class), eq(StatusLoanType.INVALID), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(true);

        assertThatThrownBy(() -> loanService.create(loan))
                .isInstanceOf(LoanService.EquipmentNotAvailableException.class);
        verify(loanDao, never()).save(any(Loan.class));
    }

    @Test
    void validate_passe_le_statut_a_valid_et_renseigne_le_validateur() throws Exception {
        Loan loan = new Loan();
        loan.setStatusType(StatusLoanType.IN_PROGRESS);
        AppUser validator = new AppUser();
        validator.setId(2);
        when(loanDao.findById(7)).thenReturn(Optional.of(loan));
        when(appUserDao.getReferenceById(2)).thenReturn(validator);

        loanService.validate(7, 2);

        assertThat(loan.getStatusType()).isEqualTo(StatusLoanType.VALID);
        assertThat(loan.getValidator()).isEqualTo(validator);
        verify(loanDao).save(loan);
    }

    @Test
    void invalidate_passe_le_statut_a_invalid() throws Exception {
        Loan loan = new Loan();
        loan.setStatusType(StatusLoanType.IN_PROGRESS);
        when(loanDao.findById(7)).thenReturn(Optional.of(loan));

        loanService.invalidate(7);

        assertThat(loan.getStatusType()).isEqualTo(StatusLoanType.INVALID);
        verify(loanDao).save(loan);
    }

    @Test
    void return_passe_a_termine_et_renseigne_la_date_de_retour() throws Exception {
        Loan loan = new Loan();
        loan.setStatusType(StatusLoanType.VALID);
        when(loanDao.findById(7)).thenReturn(Optional.of(loan));

        loanService.returnEquipment(7);

        assertThat(loan.getStatusType()).isEqualTo(StatusLoanType.TERMINE);
        assertThat(loan.getRealEndDate()).isEqualTo(LocalDate.now());
        verify(loanDao).save(loan);
    }

    @Test
    void return_refuse_si_emprunt_non_valide() {
        Loan loan = new Loan();
        loan.setStatusType(StatusLoanType.IN_PROGRESS);
        when(loanDao.findById(7)).thenReturn(Optional.of(loan));

        assertThatThrownBy(() -> loanService.returnEquipment(7))
                .isInstanceOf(LoanService.InvalidReturnException.class);
        verify(loanDao, never()).save(any(Loan.class));
    }
}
