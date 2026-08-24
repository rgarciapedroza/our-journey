package com.ourjourney.backend.dto;

import com.ourjourney.backend.entity.TripMemberRole;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TripMemberResponse {

    private Long userId;
    private String name;
    private String email;
    private String profilePicture;
    private TripMemberRole role;
}