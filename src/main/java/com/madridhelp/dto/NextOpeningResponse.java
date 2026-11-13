package com.madridhelp.dto;

import java.time.LocalDateTime;

/** The place that will open soonest, and the exact moment it opens (Madrid time). */
public record NextOpeningResponse(HelpPlaceResponse place, LocalDateTime opensAt) {
}
