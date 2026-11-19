package com.madridhelp.service;

import com.madridhelp.model.HelpPlace;
import com.madridhelp.model.Schedule;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

/**
 * Pure time rules for opening hours. No Spring and no database: you give it data and it answers.
 * That makes it trivial to unit test (task 6a).
 *
 * isOpenAt - rules from the three-row table of the planning session:
 *   Row 1 - today's slot, normal (opensAt < closesAt):      opensAt <= now < closesAt
 *   Row 2 - today's slot, crosses midnight (closes < opens): now >= opensAt   (first half of the slot)
 *   Row 3 - YESTERDAY's slot, crosses midnight:              now < closesAt   (second half of the slot)
 *
 * A slot belongs to the day it STARTS on. A slot that crosses midnight therefore affects two days:
 * the day it starts (row 2) and the day after (row 3). Row 3 only applies to slots that really
 * cross midnight: a normal Tuesday 12:00-15:00 slot must not keep the place open on Wednesday morning.
 *
 * nextOpening - only looks at when a slot STARTS (opensAt on its day), and a slot that crosses
 * midnight still starts on a single, well-defined day.
 */
public final class OpeningRules {

    private OpeningRules() {
        // Static helper, not meant to be instantiated.
    }

    public static boolean isOpenAt(HelpPlace place, LocalDateTime moment) {
        DayOfWeek today = moment.getDayOfWeek();
        DayOfWeek yesterday = today.minus(1); // minus(1) wraps around: Monday -> Sunday
        LocalTime time = moment.toLocalTime();

        // A slot belongs to the day it starts, so validity is checked against that day:
        // today's slots against today, and the second half of a night slot against yesterday.
        LocalDate todayDate = moment.toLocalDate();
        boolean validToday = isValidOn(place, todayDate);
        boolean validYesterday = isValidOn(place, todayDate.minusDays(1));

        for (Schedule schedule : place.getSchedules()) {
            if (schedule.getDayOfWeek() == today && validToday) {
                if (schedule.crossesMidnight()) {
                    // Row 2: first half of a night slot, from opening time until midnight.
                    if (!time.isBefore(schedule.getOpensAt())) {
                        return true;
                    }
                } else if (isWithin(schedule, time)) {
                    // Row 1: a normal slot.
                    return true;
                }
            } else if (schedule.getDayOfWeek() == yesterday
                    && validYesterday
                    && schedule.crossesMidnight()
                    && time.isBefore(schedule.getClosesAt())) {
                // Row 3: second half of yesterday's night slot, from midnight until closing time.
                return true;
            }
        }
        return false;
    }

    /**
     * The earliest moment strictly AFTER {@code moment} at which one of the place's slots starts.
     * Strictly after: a slot that starts exactly now does not count (it is opening right now,
     * which is isOpenAt's business). Empty if the place has no schedules at all.
     */
    public static Optional<LocalDateTime> nextOpening(HelpPlace place, LocalDateTime moment) {
        LocalDate today = moment.toLocalDate();
        LocalDateTime earliest = null;

        for (Schedule schedule : place.getSchedules()) {
            // Days from today until the next date that falls on the slot's weekday (0 = today).
            int daysAhead = (schedule.getDayOfWeek().getValue() - today.getDayOfWeek().getValue() + 7) % 7;
            LocalDateTime candidate = LocalDateTime.of(today.plusDays(daysAhead), schedule.getOpensAt());

            // Today's slot already started (or starts exactly now): the next one is a week later.
                       if (!candidate.isAfter(moment)) {
                candidate = candidate.plusDays(7);
            }

            // Seasonal place that has not started yet: jump to the first day on or after validFrom
            // that falls on this slot's weekday. It is always later than the candidate above.
            LocalDate validFrom = place.getValidFrom();
            if (validFrom != null && candidate.toLocalDate().isBefore(validFrom)) {
                int daysFromStart =
                        (schedule.getDayOfWeek().getValue() - validFrom.getDayOfWeek().getValue() + 7) % 7;
                candidate = LocalDateTime.of(validFrom.plusDays(daysFromStart), schedule.getOpensAt());
            }

            // Seasonal place that has ended: this slot never opens again.
            LocalDate validUntil = place.getValidUntil();
            if (validUntil != null && candidate.toLocalDate().isAfter(validUntil)) {
                continue;
            }

            if (earliest == null || candidate.isBefore(earliest)) {
                earliest = candidate;
            }
        }
        return Optional.ofNullable(earliest);
    }

    /**
     * Is the place in force on this day? Both limits are inclusive and optional (null = no limit).
     * Used for seasonal places such as the cold-weather campaign (23 Nov to 31 Mar).
     */
    private static boolean isValidOn(HelpPlace place, LocalDate day) {
        boolean started = place.getValidFrom() == null || !day.isBefore(place.getValidFrom());
        boolean notEnded = place.getValidUntil() == null || !day.isAfter(place.getValidUntil());
        return started && notEnded;
    }

    /** opensAt is inclusive, closesAt is exclusive: at exactly 15:00 a 12:00-15:00 slot is closed. */
    private static boolean isWithin(Schedule schedule, LocalTime time) {
        return !time.isBefore(schedule.getOpensAt()) && time.isBefore(schedule.getClosesAt());
    }
}