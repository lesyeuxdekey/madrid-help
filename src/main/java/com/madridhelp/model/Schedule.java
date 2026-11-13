package com.madridhelp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * One opening slot: one row per day and per time range.
 * This is the "many" side of the one-to-many relationship with HelpPlace.
 *
  * Convention: if closesAt is NOT after opensAt, the slot crosses midnight
 * (example: MONDAY 21:00 -> 08:00 ends on Tuesday morning).
 * The slot belongs to the day it starts on.
 *
 * Special case: opensAt equal to closesAt means a slot of a full 24 hours starting at opensAt.
 * A place open all day, every day, is stored as 00:00 -> 00:00 on each day. Do NOT use 00:00 -> 23:59
 * for that: closesAt is exclusive, so it would leave a one-minute gap every night where an open
 * place is reported as closed.
 */
@Entity
@Table(name = "schedule")
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Foreign key help_place_id. LAZY: the place is not loaded until it is actually needed.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "help_place_id", nullable = false)
    private HelpPlace helpPlace;

    // DayOfWeek is a Java enum (MONDAY..SUNDAY), which avoids free-text mistakes.
    // Stored as text so it is readable in the database and safe against reordering.
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @NotNull
    @Column(name = "opens_at", nullable = false)
    private LocalTime opensAt;

    @NotNull
    @Column(name = "closes_at", nullable = false)
    private LocalTime closesAt;

    protected Schedule() {
        // Required by JPA.
    }

    public Schedule(DayOfWeek dayOfWeek, LocalTime opensAt, LocalTime closesAt) {
        this.dayOfWeek = dayOfWeek;
        this.opensAt = opensAt;
        this.closesAt = closesAt;
    }

    /**
     * true if the slot ends on the following day (closesAt not after opensAt).
     * This includes the 24-hour case (opensAt equal to closesAt), which ends exactly 24 hours later.
     */
    public boolean crossesMidnight() {
        return !closesAt.isAfter(opensAt);
    }

    public Long getId() { return id; }

    public HelpPlace getHelpPlace() { return helpPlace; }
    void setHelpPlace(HelpPlace helpPlace) { this.helpPlace = helpPlace; }

    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public LocalTime getOpensAt() { return opensAt; }
    public LocalTime getClosesAt() { return closesAt; }
}
