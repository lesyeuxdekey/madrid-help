package com.madridhelp.service;

import com.madridhelp.model.HelpPlace;
import com.madridhelp.model.Schedule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * nextOpening with the validity period (validFrom / validUntil).
 *
 * Seasonal shelter used in most tests: open every night 21:30 -> 09:30,
 * valid from Monday 2026-11-23 to Wednesday 2027-03-31, both days included.
 */
class NextOpeningValidityTest {

    private static final LocalDate FIRST_DAY = LocalDate.of(2026, 11, 23); // Monday
    private static final LocalDate LAST_DAY = LocalDate.of(2027, 3, 31);   // Wednesday

    private static HelpPlace nightShelter(LocalDate validFrom, LocalDate validUntil) {
        HelpPlace place = new HelpPlace();
        place.setValidFrom(validFrom);
        place.setValidUntil(validUntil);
        for (DayOfWeek day : DayOfWeek.values()) {
            place.addSchedule(new Schedule(day, LocalTime.of(21, 30), LocalTime.of(9, 30)));
        }
        return place;
    }

    private static HelpPlace mondayLunch(LocalDate validFrom, LocalDate validUntil) {
        HelpPlace place = new HelpPlace();
        place.setValidFrom(validFrom);
        place.setValidUntil(validUntil);
        place.addSchedule(new Schedule(DayOfWeek.MONDAY, LocalTime.of(12, 0), LocalTime.of(15, 0)));
        return place;
    }

    @Test
    @DisplayName("Before the season starts, the next opening is the first evening of the season")
    void beforeTheSeasonStarts() {
        HelpPlace place = nightShelter(FIRST_DAY, LAST_DAY);
        Optional<LocalDateTime> next = OpeningRules.nextOpening(place, LocalDateTime.of(2026, 11, 19, 12, 0));
        assertEquals(Optional.of(LocalDateTime.of(2026, 11, 23, 21, 30)), next);
    }

    @Test
    @DisplayName("On the first day of the season, the next opening is that evening")
    void onTheFirstDay() {
        HelpPlace place = nightShelter(FIRST_DAY, LAST_DAY);
        Optional<LocalDateTime> next = OpeningRules.nextOpening(place, LocalDateTime.of(2026, 11, 23, 10, 0));
        assertEquals(Optional.of(LocalDateTime.of(2026, 11, 23, 21, 30)), next);
    }

    @Test
    @DisplayName("On the last day before the evening slot, it is still coming")
    void onTheLastDayBeforeTheSlot() {
        HelpPlace place = nightShelter(FIRST_DAY, LAST_DAY);
        Optional<LocalDateTime> next = OpeningRules.nextOpening(place, LocalDateTime.of(2027, 3, 31, 20, 0));
        assertEquals(Optional.of(LocalDateTime.of(2027, 3, 31, 21, 30)), next);
    }

    @Test
    @DisplayName("On the last day after the evening slot started, there is no next opening")
    void onTheLastDayAfterTheSlot() {
        HelpPlace place = nightShelter(FIRST_DAY, LAST_DAY);
        Optional<LocalDateTime> next = OpeningRules.nextOpening(place, LocalDateTime.of(2027, 3, 31, 22, 0));
        assertEquals(Optional.empty(), next);
    }

    @Test
    @DisplayName("After the season ended, there is no next opening")
    void afterTheSeason() {
        HelpPlace place = nightShelter(FIRST_DAY, LAST_DAY);
        Optional<LocalDateTime> next = OpeningRules.nextOpening(place, LocalDateTime.of(2027, 4, 15, 12, 0));
        assertEquals(Optional.empty(), next);
    }

    @Test
    @DisplayName("Without limits it behaves as before")
    void withoutLimits() {
        HelpPlace place = nightShelter(null, null);
        Optional<LocalDateTime> next = OpeningRules.nextOpening(place, LocalDateTime.of(2030, 6, 15, 22, 0));
        assertEquals(Optional.of(LocalDateTime.of(2030, 6, 16, 21, 30)), next);
    }

    @Test
    @DisplayName("A Monday-only slot with validFrom on a Wednesday jumps to the following Monday")
    void weekdayDoesNotMatchTheStartDate() {
        HelpPlace place = mondayLunch(LocalDate.of(2026, 11, 25), null); // Wednesday
        Optional<LocalDateTime> next = OpeningRules.nextOpening(place, LocalDateTime.of(2026, 11, 19, 9, 0));
        assertEquals(Optional.of(LocalDateTime.of(2026, 11, 30, 12, 0)), next);
    }

    @Test
    @DisplayName("A Monday-only slot whose season ends before the next Monday has no next opening")
    void nextMondayIsAfterTheEnd() {
        HelpPlace place = mondayLunch(null, LocalDate.of(2026, 11, 25)); // Wednesday
        Optional<LocalDateTime> next = OpeningRules.nextOpening(place, LocalDateTime.of(2026, 11, 24, 13, 0));
        assertEquals(Optional.empty(), next);
    }
}