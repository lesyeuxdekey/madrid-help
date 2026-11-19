package com.madridhelp.config;

import com.madridhelp.model.HelpPlace;
import com.madridhelp.model.PlaceType;
import com.madridhelp.model.Schedule;
import com.madridhelp.repository.HelpPlaceRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads FICTIONAL test data when the app runs with the "dev" profile.
 *
 * None of these places exist. This app is meant for people in a vulnerable situation, so
 * invented data must never look real: every name starts with "TEST". Real data will be
 * imported and verified separately (user story H0).
 *
 * The cases are chosen on purpose to exercise the "open now" logic (task 4):
 * simple slot, two shifts in one day, weekend only, and a slot that crosses midnight.
 */
@Component
@Profile("dev")
public class DevDataLoader implements CommandLineRunner {

    private final HelpPlaceRepository repository;

    public DevDataLoader(HelpPlaceRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }

        // 1. Simple case: one slot, Monday to Friday.
        HelpPlace simple = place("TEST Soup Kitchen A (simple)", PlaceType.SOUP_KITCHEN,
                "Test address 1", 40.4168, -3.7038,
                weekdays(LocalTime.of(12, 0), LocalTime.of(15, 0)));

        // 2. Two shifts on the same day = two rows with the same day.
        List<Schedule> twoShifts = new ArrayList<>();
        twoShifts.addAll(weekdays(LocalTime.of(12, 0), LocalTime.of(15, 0)));
        twoShifts.addAll(weekdays(LocalTime.of(19, 0), LocalTime.of(21, 0)));
        HelpPlace doubleShift = place("TEST Soup Kitchen B (two shifts)", PlaceType.SOUP_KITCHEN,
                "Test address 2", 40.4065, -3.6895, twoShifts);

        // 3. Weekend only.
        HelpPlace weekend = place("TEST Soup Kitchen C (weekend)", PlaceType.SOUP_KITCHEN,
                "Test address 3", 40.4460, -3.6920,
                days(LocalTime.of(13, 0), LocalTime.of(14, 30), DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));

        // 4. Crosses midnight: closesAt (08:00) is before opensAt (21:00).
               HelpPlace night = place("TEST Shelter D (crosses midnight)", PlaceType.SHELTER,
                "Test address 4", 40.4659, -3.6890,
                days(LocalTime.of(21, 0), LocalTime.of(8, 0), DayOfWeek.values()));
        // Not walk-in: this is how the app will show access conditions on a card.
        night.setAccessNote("Acceso por derivación de los servicios sociales. No se puede entrar sin derivación.");

        // 5. A few days a week.
        // 5. A few days a week.
        HelpPlace shower = place("TEST Showers E", PlaceType.SHOWER,
                "Test address 5", 40.3910, -3.6950,
                days(LocalTime.of(9, 0), LocalTime.of(12, 0),
                        DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY));

        // 6. Another type, to check the type filter.
        HelpPlace clothing = place("TEST Clothing F", PlaceType.CLOTHING,
                "Test address 6", 40.4350, -3.7190,
                days(LocalTime.of(10, 0), LocalTime.of(13, 0), DayOfWeek.TUESDAY, DayOfWeek.THURSDAY));

        repository.saveAll(List.of(simple, doubleShift, weekend, night, shower, clothing));
    }

    private HelpPlace place(String name, PlaceType type, String address,
                            double latitude, double longitude, List<Schedule> schedules) {
        HelpPlace place = new HelpPlace();
        place.setName(name);
        place.setType(type);
        place.setAddress(address);
        place.setLatitude(latitude);
        place.setLongitude(longitude);
        schedules.forEach(place::addSchedule);
        return place;
    }

    private List<Schedule> weekdays(LocalTime opensAt, LocalTime closesAt) {
        return days(opensAt, closesAt, DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);
    }

    private List<Schedule> days(LocalTime opensAt, LocalTime closesAt, DayOfWeek... days) {
        List<Schedule> schedules = new ArrayList<>();
        for (DayOfWeek day : days) {
            schedules.add(new Schedule(day, opensAt, closesAt));
        }
        return schedules;
    }
}
