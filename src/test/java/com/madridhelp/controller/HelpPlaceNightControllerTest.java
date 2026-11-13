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

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Same idea as HelpPlaceControllerTest, but with the clock frozen at FRIDAY 07:30 Madrid time.
 *
 * At that moment the only place open is shelter D (TEST Shelter D), whose slot started on
 * Thursday at 21:00 and ends on Friday at 08:00. It is the second half of a night slot
 * (row 3 of the rules table). The soup kitchens (from 12:00) and the showers (from 09:00)
 * are still closed.
 */
@SpringBootTest
@AutoConfigureMockMvc
class HelpPlaceNightControllerTest {

    @TestConfiguration
    static class FixedClockConfig {

        @Bean
        @Primary
        Clock fixedClock() {
            // 07:30 in Madrid in November (UTC+1) is 06:30 UTC.
            return Clock.fixed(Instant.parse("2026-11-13T06:30:00Z"), ZoneId.of("Europe/Madrid"));
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/places?openNow=true on Friday 07:30 returns only the night shelter")
    void nightShelterIsOpenInTheEarlyMorning() throws Exception {
        mockMvc.perform(get("/api/places").param("openNow", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("TEST Shelter D (crosses midnight)"));
    }

    @Test
    @DisplayName("GET /api/places?type=SHELTER&openNow=true on Friday 07:30 returns the shelter")
    void shelterFilterCombinedWithOpenNow() throws Exception {
        mockMvc.perform(get("/api/places").param("type", "SHELTER").param("openNow", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/places?type=SOUP_KITCHEN&openNow=true on Friday 07:30 returns nothing")
    void soupKitchensAreStillClosed() throws Exception {
        mockMvc.perform(get("/api/places").param("type", "SOUP_KITCHEN").param("openNow", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}