package com.ourjourney.backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TripResponse {
    
    private Long id;
    private String name;
    private String description;
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private String coverImage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
}
