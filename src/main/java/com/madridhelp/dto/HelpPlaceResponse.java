package com.madridhelp.dto;

import com.madridhelp.model.HelpPlace;
import com.madridhelp.model.PlaceType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * What the API returns for one place.
 *
 * We return this instead of the HelpPlace entity because the entity and Schedule point at each
 * other (HelpPlace -> Schedule -> HelpPlace), which would make the JSON infinitely recursive,
 * and because the API should not be tied to the database model.
 */
public record HelpPlaceResponse(
        Long id,
        String name,
        PlaceType type,
        String address,
        double latitude,
        double longitude,
        String phone,
        LocalDateTime lastUpdated,
        List<ScheduleResponse> schedules) {

    public static HelpPlaceResponse from(HelpPlace place) {
        return new HelpPlaceResponse(
                place.getId(),
                place.getName(),
                place.getType(),
                place.getAddress(),
                place.getLatitude(),
                place.getLongitude(),
                place.getPhone(),
                place.getLastUpdated(),
                place.getSchedules().stream().map(ScheduleResponse::from).toList());
    }
}
