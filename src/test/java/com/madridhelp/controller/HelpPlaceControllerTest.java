package com.madridhelp.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests the real HTTP endpoints with the whole application running (in memory).
 *
 * The clock is frozen at Thursday 12 November 2026, 13:30 Madrid time, so the results never
 * depend on when you run the tests. The data comes from DevDataLoader (the "dev" profile is
 * active in application.properties), so if you change that data, update these expectations.
 */
@SpringBootTest
@AutoConfigureMockMvc
class HelpPlaceControllerTest {

    @TestConfiguration
    static class FixedClockConfig {

        // @Primary makes Spring inject this clock instead of the real one from ClockConfig.
        @Bean
        @Primary
        Clock fixedClock() {
            // 13:30 in Madrid in November (UTC+1) is 12:30 UTC.
            return Clock.fixed(Instant.parse("2026-11-12T12:30:00Z"), ZoneId.of("Europe/Madrid"));
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/places returns the 6 test places")
    void listsAllPlaces() throws Exception {
        mockMvc.perform(get("/api/places"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(6)));
    }

    @Test
    @DisplayName("GET /api/places?type=SOUP_KITCHEN returns only the 3 soup kitchens")
    void filtersByType() throws Exception {
        mockMvc.perform(get("/api/places").param("type", "SOUP_KITCHEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    @DisplayName("GET /api/places?openNow=true on Thursday 13:30 returns only kitchens A and B")
    void filtersByOpenNow() throws Exception {
        mockMvc.perform(get("/api/places").param("openNow", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", containsInAnyOrder(
                        "TEST Soup Kitchen A (simple)",
                        "TEST Soup Kitchen B (two shifts)")));
    }

    @Test
    @DisplayName("An invalid type answers 400 Bad Request")
    void invalidTypeIsRejected() throws Exception {
        mockMvc.perform(get("/api/places").param("type", "FOO"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/places/next-opening on Thursday 13:30 is kitchen B, today at 19:00")
    void nextOpeningOverall() throws Exception {
        mockMvc.perform(get("/api/places/next-opening"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.place.name").value("TEST Soup Kitchen B (two shifts)"))
                .andExpect(jsonPath("$.opensAt").value("2026-11-12T19:00:00"));
    }

    @Test
    @DisplayName("GET /api/places/next-opening?type=SHOWER is showers E, tomorrow at 09:00")
    void nextOpeningByType() throws Exception {
        mockMvc.perform(get("/api/places/next-opening").param("type", "SHOWER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.place.name").value("TEST Showers E"))
                .andExpect(jsonPath("$.opensAt").value("2026-11-13T09:00:00"));
    }
}
