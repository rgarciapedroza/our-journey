package com.ourjourney.backend.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ItineraryItemResponse {
    private final Long id;
    private final Long tripId;

    private final Long createdById;
    private final String createdByName;
    private final String createdByProfilePicture;

    private final LocalDate activityDate;
    private final LocalTime startTime;
    private final LocalTime endTime;

    private final String title;
    private final String description;
    private final String place;

    private final Instant createdAt;
    private final Instant updatedAt;
}
