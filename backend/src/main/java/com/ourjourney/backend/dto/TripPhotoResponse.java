package com.ourjourney.backend.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TripPhotoResponse {
    
    private final Long id;
    private final String imageUrl;
    private final String caption;
    private final Long uploadedById;
    private final String uploadedByName;
    private final String uploadedByProfilePicture;
    private final LocalDateTime createdAt;
    
}
