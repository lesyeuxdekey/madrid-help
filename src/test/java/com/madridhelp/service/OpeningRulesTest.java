package com.madridhelp.service;

import com.madridhelp.model.HelpPlace;
import com.madridhelp.model.Schedule;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.DayOfWeek.TUESDAY;
import static java.time.DayOfWeek.WEDNESDAY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the pure opening-hours rules. No Spring, no database: they run in milliseconds.
 *
 * Each test has three parts (Arrange - Act - Assert): build the data, call the rule, check the answer.
 */
class OpeningRulesTest {

    // Every test lives in the week of Monday 9 November 2026, so the days are easy to read:
    // at(THURSDAY, 12, 0) is Thursday 12 November 2026, 12:00.
    private static LocalDateTime at(DayOfWeek day, int hour, int minute) {
        return LocalDateTime.of(2026, 11, 9, hour, minute).plusDays(day.getValue() - 1);
    }

    private static HelpPlace placeWith(Schedule... schedules) {
        HelpPlace place = new HelpPlace();
        for (Schedule schedule : schedules) {
            place.addSchedule(schedule);
        }
        return place;
    }

    private static Schedule slot(DayOfWeek day, int openHour, int openMinute, int closeHour, int closeMinute) {
        return new Schedule(day, LocalTime.of(openHour, openMinute), LocalTime.of(closeHour, closeMinute));
    }

    /** Soup kitchen open Monday to Friday, 12:00-15:00. */
    private static HelpPlace weekdayKitchen() {
        return placeWith(
                slot(MONDAY, 12, 0, 15, 0), slot(TUESDAY, 12, 0, 15, 0), slot(WEDNESDAY, 12, 0, 15, 0),
                slot(THURSDAY, 12, 0, 15, 0), slot(FRIDAY, 12, 0, 15, 0));
    }

    // ---------------------------------------------------------------- isOpenAt (task 4a)

    @Test
    @DisplayName("Open exactly at the opening time (opensAt is inclusive)")
    void openExactlyAtOpeningTime() {
        assertTrue(OpeningRules.isOpenAt(weekdayKitchen(), at(THURSDAY, 12, 0)));
    }

    @Test
    @DisplayName("Closed one minute before opening")
    void closedOneMinuteBeforeOpening() {
        assertFalse(OpeningRules.isOpenAt(weekdayKitchen(), at(THURSDAY, 11, 59)));
    }

    @Test
    @DisplayName("Open during the last minute")
    void openDuringLastMinute() {
        assertTrue(OpeningRules.isOpenAt(weekdayKitchen(), at(THURSDAY, 14, 59)));
    }

    @Test
    @DisplayName("Closed exactly at the closing time (closesAt is exclusive)")
    void closedExactlyAtClosingTime() {
        assertFalse(OpeningRules.isOpenAt(weekdayKitchen(), at(THURSDAY, 15, 0)));
    }

    @Test
    @DisplayName("Closed on a day without a slot")
    void closedOnDayWithoutSlot() {
        assertFalse(OpeningRules.isOpenAt(weekdayKitchen(), at(SATURDAY, 13, 0)));
    }

    @Test
    @DisplayName("Two shifts in one day: closed between them, open in the second")
    void twoShiftsSameDay() {
        HelpPlace twoShifts = placeWith(slot(THURSDAY, 12, 0, 15, 0), slot(THURSDAY, 19, 0, 21, 0));

        assertFalse(OpeningRules.isOpenAt(twoShifts, at(THURSDAY, 17, 0)));
        assertTrue(OpeningRules.isOpenAt(twoShifts, at(THURSDAY, 20, 0)));
    }

    @Test
    @DisplayName("A place with no schedules is never open")
    void noSchedulesIsClosed() {
        assertFalse(OpeningRules.isOpenAt(placeWith(), at(THURSDAY, 13, 0)));
    }

    @Test
    @DisplayName("Regression guard for 4b: a NORMAL slot from yesterday must not keep the place open after midnight")
    void normalSlotYesterdayDoesNotCountToday() {
        // Tuesday 12:00-15:00 is NOT a midnight-crossing slot. On Wednesday 07:30 the place is closed.
        // A careless 4b implementation ("yesterday's slot and now < closesAt") would wrongly say open.
        HelpPlace tuesdayLunch = placeWith(slot(TUESDAY, 12, 0, 15, 0));

        assertFalse(OpeningRules.isOpenAt(tuesdayLunch, at(WEDNESDAY, 7, 30)));
    }

    // ---------------------------------------------------------------- isOpenAt (task 4b, pending)

    @Test
    @Disabled("Pending task 4b: slots that cross midnight (row 2 of the rules table)")
    @DisplayName("4b: a night slot is open before midnight on the day it starts")
    void nightSlotOpenBeforeMidnight() {
        HelpPlace shelter = placeWith(slot(TUESDAY, 21, 0, 8, 0));

        assertTrue(OpeningRules.isOpenAt(shelter, at(TUESDAY, 22, 0)));
    }

    @Test
    @Disabled("Pending task 4b: slots that cross midnight (row 3 of the rules table)")
    @DisplayName("4b: a night slot is still open the next morning, until closing time")
    void nightSlotOpenNextMorning() {
        HelpPlace shelter = placeWith(slot(TUESDAY, 21, 0, 8, 0));

        assertTrue(OpeningRules.isOpenAt(shelter, at(WEDNESDAY, 7, 30)));
        assertFalse(OpeningRules.isOpenAt(shelter, at(WEDNESDAY, 8, 0)));
    }

    // ---------------------------------------------------------------- nextOpening (task 5)

    @Test
    @DisplayName("Next opening: later today when the slot has not started yet")
    void nextOpeningLaterToday() {
        assertEquals(Optional.of(at(THURSDAY, 12, 0)),
                OpeningRules.nextOpening(weekdayKitchen(), at(THURSDAY, 11, 0)));
    }

    @Test
    @DisplayName("Next opening: a slot that starts exactly now does not count (strictly after)")
    void nextOpeningStrictlyAfter() {
        assertEquals(Optional.of(at(FRIDAY, 12, 0)),
                OpeningRules.nextOpening(weekdayKitchen(), at(THURSDAY, 12, 0)));
    }

    @Test
    @DisplayName("Next opening: after closing time it is tomorrow")
    void nextOpeningAfterClosing() {
        assertEquals(Optional.of(at(FRIDAY, 12, 0)),
                OpeningRules.nextOpening(weekdayKitchen(), at(THURSDAY, 15, 0)));
    }

    @Test
    @DisplayName("Next opening: Friday evening jumps to Monday of the following week")
    void nextOpeningSkipsWeekend() {
        assertEquals(Optional.of(at(MONDAY, 12, 0).plusWeeks(1)),
                OpeningRules.nextOpening(weekdayKitchen(), at(FRIDAY, 16, 0)));
    }

    @Test
    @DisplayName("Next opening: a place that only opens on Thursdays, asked on Thursday afternoon, is a week away")
    void nextOpeningNextWeek() {
        HelpPlace onlyThursdays = placeWith(slot(THURSDAY, 12, 0, 15, 0));

        assertEquals(Optional.of(at(THURSDAY, 12, 0).plusWeeks(1)),
                OpeningRules.nextOpening(onlyThursdays, at(THURSDAY, 13, 0)));
    }

    @Test
    @DisplayName("Next opening: a midnight-crossing slot counts from the moment it starts")
    void nextOpeningOfNightSlot() {
        HelpPlace shelter = placeWith(slot(THURSDAY, 21, 0, 8, 0));

        assertEquals(Optional.of(at(THURSDAY, 21, 0)),
                OpeningRules.nextOpening(shelter, at(THURSDAY, 13, 30)));
    }

    @Test
    @DisplayName("Next opening: empty when the place has no schedules")
    void nextOpeningEmptyWithoutSchedules() {
        assertEquals(Optional.empty(), OpeningRules.nextOpening(placeWith(), at(THURSDAY, 13, 0)));
    }
}
