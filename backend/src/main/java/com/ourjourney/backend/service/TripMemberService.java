package com.ourjourney.backend.service;

import java.util.List;

import com.ourjourney.backend.dto.AddTripMemberRequest;
import com.ourjourney.backend.dto.TripMemberResponse;

public interface TripMemberService {

    List<TripMemberResponse> getMembers(Long tripId);

    TripMemberResponse addMember(
             Long tripId,
            AddTripMemberRequest request,
            String currentUserEmail
    );

    void removeMember(
             Long tripId,
             Long userId,
            String currentUserEmail
    );
}