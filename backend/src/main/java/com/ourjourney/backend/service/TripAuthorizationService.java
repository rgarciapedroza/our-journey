package com.ourjourney.backend.service;

import com.ourjourney.backend.entity.TripMember;

public interface TripAuthorizationService {
    public TripMember getCurrentMember(
            Long tripId,
            String email
    );
}
