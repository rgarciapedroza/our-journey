package com.ourjourney.backend.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItineraryItemRequest{
    @NotNull(message = "Activity date is required")
    private LocalDate activityDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    private LocalTime endTime;

    @NotBlank(message = "Title is required")
    @Size(max = 120, message = "Title must not exceed 120 characters")
    private String title;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Size(max = 250, message = "Place must not exceed 250 characters")
    private String place;
}