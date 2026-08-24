package com.ourjourney.backend.dto;

public record UserSearchResponse(
        Long id,
        String name,
        String email,
        String profilePicture
) {
}
