package com.madridhelp.service;

import com.madridhelp.model.HelpPlace;
import com.madridhelp.model.Schedule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validity period (validFrom / validUntil) of seasonal places.
 *
 * Model used here: a cold-weather shelter open every night 21:30 -> 09:30,
 * valid from Monday 2026-11-23 to Wednesday 2027-03-31, both days included.
 * A slot belongs to the day it STARTS, so the limits are checked against the start day.
 */
class OpeningRulesValidityTest {

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

    private static LocalDateTime at(LocalDate day, int hour, int minute) {
        return day.atTime(hour, minute);
    }

    @Test
    @DisplayName("The evening before validFrom the place is still closed")
    void closedTheEveningBeforeTheFirstDay() {
        HelpPlace place = nightShelter(FIRST_DAY, LAST_DAY);
        assertFalse(OpeningRules.isOpenAt(place, at(FIRST_DAY.minusDays(1), 23, 0)));
    }

    @Test
    @DisplayName("On validFrom in the early morning it is closed: that night slot started the day before")
    void closedTheMorningOfTheFirstDay() {
        HelpPlace place = nightShelter(FIRST_DAY, LAST_DAY);
        assertFalse(OpeningRules.isOpenAt(place, at(FIRST_DAY, 8, 0)));
    }

    @Test
    @DisplayName("On validFrom in the evening it opens")
    void openTheEveningOfTheFirstDay() {
        HelpPlace place = nightShelter(FIRST_DAY, LAST_DAY);
        assertTrue(OpeningRules.isOpenAt(place, at(FIRST_DAY, 22, 0)));
    }

    @Test
    @DisplayName("The morning after validFrom it is open (second half of the first slot)")
    void openTheMorningAfterTheFirstDay() {
        HelpPlace place = nightShelter(FIRST_DAY, LAST_DAY);
        assertTrue(OpeningRules.isOpenAt(place, at(FIRST_DAY.plusDays(1), 8, 0)));
    }

    @Test
    @DisplayName("On validUntil in the evening it is still open (limit is inclusive)")
    void openTheEveningOfTheLastDay() {
        HelpPlace place = nightShelter(FIRST_DAY, LAST_DAY);
        assertTrue(OpeningRules.isOpenAt(place, at(LAST_DAY, 22, 0)));
    }

    @Test
    @DisplayName("The morning after validUntil it is open: that slot started on the last valid day")
    void openTheMorningAfterTheLastDay() {
        HelpPlace place = nightShelter(FIRST_DAY, LAST_DAY);
        assertTrue(OpeningRules.isOpenAt(place, at(LAST_DAY.plusDays(1), 8, 0)));
    }

    @Test
    @DisplayName("The evening after validUntil it is closed")
    void closedTheEveningAfterTheLastDay() {
        HelpPlace place = nightShelter(FIRST_DAY, LAST_DAY);
        assertFalse(OpeningRules.isOpenAt(place, at(LAST_DAY.plusDays(1), 22, 0)));
    }

    @Test
    @DisplayName("Without limits the place is open on any date")
    void noLimitsMeansAlwaysInForce() {
        HelpPlace place = nightShelter(null, null);
        assertTrue(OpeningRules.isOpenAt(place, at(LocalDate.of(2030, 6, 15), 22, 0)));
    }

    @Test
    @DisplayName("Only validUntil set: open before it, closed after it")
    void onlyAnEndLimit() {
        HelpPlace place = nightShelter(null, LAST_DAY);
        assertTrue(OpeningRules.isOpenAt(place, at(LocalDate.of(2020, 1, 15), 22, 0)));
        assertFalse(OpeningRules.isOpenAt(place, at(LAST_DAY.plusDays(1), 22, 0)));
    }

    @Test
    @DisplayName("Only validFrom set: closed before it, open after it")
    void onlyAStartLimit() {
        HelpPlace place = nightShelter(FIRST_DAY, null);
        assertFalse(OpeningRules.isOpenAt(place, at(FIRST_DAY.minusDays(1), 22, 0)));
        assertTrue(OpeningRules.isOpenAt(place, at(LocalDate.of(2040, 1, 15), 22, 0)));
    }
}