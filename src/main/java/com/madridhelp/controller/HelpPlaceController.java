package com.madridhelp.controller;

import com.madridhelp.dto.HelpPlaceResponse;
import com.madridhelp.dto.NextOpeningResponse;
import com.madridhelp.model.PlaceType;
import com.madridhelp.service.HelpPlaceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/places")
public class HelpPlaceController {

    private final HelpPlaceService service;

    public HelpPlaceController(HelpPlaceService service) {
        this.service = service;
    }

    /**
     * GET /api/places                                  -> all places
     * GET /api/places?type=SOUP_KITCHEN                -> only soup kitchens
     * GET /api/places?type=SOUP_KITCHEN&openNow=true   -> only soup kitchens open right now
     * An invalid type (e.g. ?type=FOO) makes Spring answer 400 Bad Request by itself.
     */
    @GetMapping
    public List<HelpPlaceResponse> list(
            @RequestParam(required = false) PlaceType type,
            @RequestParam(defaultValue = "false") boolean openNow) {
        return service.findPlaces(type, openNow);
    }

    /**
     * GET /api/places/next-opening                     -> the place that opens soonest, and when
     * GET /api/places/next-opening?type=SHOWER         -> same, only among showers
     * It is a separate endpoint on purpose: openNow=true must never return a closed place.
     * Answers 404 if there is nothing to report.
     */
    @GetMapping("/next-opening")
    public ResponseEntity<NextOpeningResponse> nextOpening(@RequestParam(required = false) PlaceType type) {
        return service.findNextToOpen(type)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
