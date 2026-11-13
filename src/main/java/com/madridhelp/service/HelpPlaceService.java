package com.madridhelp.service;

import com.madridhelp.dto.HelpPlaceResponse;
import com.madridhelp.dto.NextOpeningResponse;
import com.madridhelp.model.HelpPlace;
import com.madridhelp.model.PlaceType;
import com.madridhelp.repository.HelpPlaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Business logic lives here. The controller only handles HTTP, the repository only handles data.
 */
@Service
public class HelpPlaceService {

    private final HelpPlaceRepository repository;
    private final Clock clock;

    public HelpPlaceService(HelpPlaceRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    /**
     * @param type     optional filter; null means all types
     * @param openNow  if true, keep only the places that are open at this moment (Madrid time)
     */
    @Transactional(readOnly = true)
    public List<HelpPlaceResponse> findPlaces(PlaceType type, boolean openNow) {
        List<HelpPlace> places = loadPlaces(type);

        if (openNow) {
            LocalDateTime now = LocalDateTime.now(clock);
            places = places.stream().filter(place -> OpeningRules.isOpenAt(place, now)).toList();
        }

        return places.stream().map(HelpPlaceResponse::from).toList();
    }

    /**
     * The place that will open soonest after this moment, with the time it opens.
     * Ties are broken by id so the answer is always the same.
     * Empty only if no place (of that type) has any schedule.
     */
    @Transactional(readOnly = true)
    public Optional<NextOpeningResponse> findNextToOpen(PlaceType type) {
        LocalDateTime now = LocalDateTime.now(clock);

        return loadPlaces(type).stream()
                .flatMap(place -> OpeningRules.nextOpening(place, now).stream()
                        .map(opensAt -> new NextOpeningResponse(HelpPlaceResponse.from(place), opensAt)))
                .min(Comparator.comparing(NextOpeningResponse::opensAt)
                        .thenComparing(response -> response.place().id()));
    }

    private List<HelpPlace> loadPlaces(PlaceType type) {
        return (type == null) ? repository.findAll() : repository.findByType(type);
    }
}
