package com.madridhelp.config;

import com.madridhelp.model.HelpPlace;
import com.madridhelp.model.PlaceType;
import com.madridhelp.model.Schedule;
import com.madridhelp.repository.HelpPlaceRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads REAL data when the app runs with the "real" profile.
 *
 * Source: Ayuntamiento de Madrid, open data set "Centros de acogida de la Red Municipal de
 * Atencion a Personas Sin Hogar" (datos.madrid.es), licence CC BY 4.0. The source must be
 * credited wherever this data is shown.
 *
 * The rows are copied by hand because the schedules in the source are free text. Rule for this
 * file: a place is only listed when the source says clearly WHEN it is open and WHO it is for.
 * Nothing is invented: if the source does not give a date, no date is set.
 *
 * NOT imported yet, pending a phone call to verify:
 *  - Pinar de San Jose (cold-weather campaign): the source gives no start or end dates.
 *  - Centro de Acogida Temporal para Inmigrantes "San Blas": it is for immigrants and the source
 *    does not explain how to get in.
 *  - Centro de Servicios Sociales Valdebernardo: appointment only, not a place to go "now".
 *
 * This loader never runs together with DevDataLoader ("dev"), so TEST and real data never mix.
 */
@Component
@Profile("real")
public class RealDataLoader implements CommandLineRunner {

    private final HelpPlaceRepository repository;

    public RealDataLoader(HelpPlaceRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }

        // Slot 00:00 -> 00:00 means open 24 hours (see the convention in Schedule).
        HelpPlace juanLuisVives = place(
                "Centro de acogida Juan Luis Vives", PlaceType.SHELTER,
                "Calle Alcalde Juan de Mata Sevillano, 18, 28052 Madrid",
                40.39880177290262, -3.584430557780187, "917 602 499",
                "Plaza por derivación del equipo Puerta Única (PUE) del servicio Madrid en Calle. "
                        + "Información y solicitud en el Punto de Primera Atención, Paseo Viejo, 1 "
                        + "(lunes a viernes, de 8 a 20 h).",
                days(LocalTime.MIDNIGHT, LocalTime.MIDNIGHT, DayOfWeek.values()));

        // The source says "Del 23 de noviembre al 31 de marzo" without a year: this is the
        // 2026-2027 campaign (the data set is current for the winter that starts in November 2026).
        HelpPlace villaDeVallecas = place(
                "Campaña contra el frío: dispositivo de Villa de Vallecas", PlaceType.SHELTER,
                "Camino Pozo del Tío Raimundo, s/n, 28031 Madrid",
                40.37403087580941, -3.651217463372421, "914 802 020",
                "Solo para hombres (140 plazas). Hay servicio especial de la EMT desde la "
                        + "Glorieta de Atocha. Información: 914 802 020.",
                days(LocalTime.of(21, 30), LocalTime.of(9, 30), DayOfWeek.values()));
        villaDeVallecas.setValidFrom(LocalDate.of(2026, 11, 23));
        villaDeVallecas.setValidUntil(LocalDate.of(2027, 3, 31));

        // The source does not say whether a referral is needed, so the note says to call first.
        HelpPlace santaMariaJosefa = place(
                "Comedor social Santa María Josefa", PlaceType.SOUP_KITCHEN,
                "Calle Encarnación González, 3, 28053 Madrid",
                40.39139820532087, -3.667827984709319, "914 771 004",
                "Dos turnos: a las 13:00 y a las 13:30. Confirma las condiciones de acceso "
                        + "llamando al 914 771 004.",
                days(LocalTime.of(13, 0), LocalTime.of(14, 30),
                        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                        DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY));

        repository.saveAll(List.of(juanLuisVives, villaDeVallecas, santaMariaJosefa));
    }

    private HelpPlace place(String name, PlaceType type, String address,
                            double latitude, double longitude, String phone,
                            String accessNote, List<Schedule> schedules) {
        HelpPlace place = new HelpPlace();
        place.setName(name);
        place.setType(type);
        place.setAddress(address);
        place.setLatitude(latitude);
        place.setLongitude(longitude);
        place.setPhone(phone);
        place.setAccessNote(accessNote);
        schedules.forEach(place::addSchedule);
        return place;
    }

    private List<Schedule> days(LocalTime opensAt, LocalTime closesAt, DayOfWeek... days) {
        List<Schedule> schedules = new ArrayList<>();
        for (DayOfWeek day : days) {
            schedules.add(new Schedule(day, opensAt, closesAt));
        }
        return schedules;
    }
}