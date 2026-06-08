package com.locmns.service;

import com.locmns.dao.EventDao;
import com.locmns.dao.LoanDao;
import com.locmns.model.Event;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Test unitaire des notifications gestionnaire : findUnread ne doit remonter
 * que les événements non lus (readingDate IS NULL), via le DAO dédié.
 */
@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock private EventDao eventDao;
    @Mock private LoanDao loanDao;

    @InjectMocks private EventService eventService;

    @Test
    void findUnread_retourne_uniquement_les_evenements_non_lus() {
        Event nonLu1 = new Event();
        Event nonLu2 = new Event();
        when(eventDao.findByReadingDateIsNull()).thenReturn(List.of(nonLu1, nonLu2));

        List<Event> result = eventService.findUnread();

        assertThat(result).containsExactly(nonLu1, nonLu2);
        assertThat(result).allMatch(e -> e.getReadingDate() == null);
    }

    @Test
    void markAsRead_renseigne_la_date_de_lecture() {
        Event event = new Event();
        when(eventDao.findById(5)).thenReturn(java.util.Optional.of(event));

        eventService.markAsRead(5);

        assertThat(event.getReadingDate()).isNotNull();
        assertThat(event.getReadingDate()).isBeforeOrEqualTo(LocalDateTime.now());
    }
}
