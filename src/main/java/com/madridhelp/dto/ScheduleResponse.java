package com.madridhelp.dto;

import com.madridhelp.model.Schedule;

import java.time.DayOfWeek;
import java.time.LocalTime;

/** What the API returns for one opening slot. A record is an immutable data holder. */
public record ScheduleResponse(DayOfWeek dayOfWeek, LocalTime opensAt, LocalTime closesAt) {

    public static ScheduleResponse from(Schedule schedule) {
        return new ScheduleResponse(schedule.getDayOfWeek(), schedule.getOpensAt(), schedule.getClosesAt());
    }
}
