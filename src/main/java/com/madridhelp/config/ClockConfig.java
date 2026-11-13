package com.madridhelp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * One shared Clock fixed to Madrid time.
 *
 * Why not call LocalDateTime.now() directly?
 * 1. A cloud server usually runs in UTC, which would be 1-2 hours off from Madrid.
 * 2. Tests can replace this Clock with Clock.fixed(...) to simulate any day and time.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("Europe/Madrid"));
    }
}
