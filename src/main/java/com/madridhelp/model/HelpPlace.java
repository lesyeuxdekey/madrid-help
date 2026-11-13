package com.madridhelp.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * A place where a person can get help (soup kitchen, shelter, shower...).
 * This is the "one" side of the one-to-many relationship with Schedule.
 *
 * Named HelpPlace (not Resource) to avoid confusion with Spring's Resource interface.
 */
@Entity
@Table(name = "help_place")
public class HelpPlace {

    private static final ZoneId MADRID = ZoneId.of("Europe/Madrid");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    // STRING stores "SHELTER" instead of 0, 1, 2...: reordering the enum won't corrupt existing data.
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlaceType type;

    @NotBlank
    @Column(nullable = false)
    private String address;

    // TODO: validate that the coordinates fall inside Madrid (we will do it with the endpoint).
    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    private String phone;

    // Key challenge from the case study: data reliability. Filled in automatically on save.
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    // mappedBy: the foreign key lives in Schedule (field "helpPlace"), not in this table.
    // orphanRemoval: removing a schedule from this list also deletes it from the database.
    @OneToMany(mappedBy = "helpPlace", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Schedule> schedules = new ArrayList<>();

    @PrePersist
    @PreUpdate
    void markUpdated() {
        this.lastUpdated = LocalDateTime.now(MADRID);
    }

    /** Keeps both sides of the relationship in sync. Always use this method. */
    public void addSchedule(Schedule schedule) {
        schedules.add(schedule);
        schedule.setHelpPlace(this);
    }

    public void removeSchedule(Schedule schedule) {
        schedules.remove(schedule);
        schedule.setHelpPlace(null);
    }

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public PlaceType getType() { return type; }
    public void setType(PlaceType type) { this.type = type; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }

    public List<Schedule> getSchedules() { return schedules; }
}
