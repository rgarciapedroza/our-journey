package com.ourjourney.backend.service;

import java.util.List;

import com.ourjourney.backend.dto.AddTripMemberRequest;
import com.ourjourney.backend.dto.TripMemberResponse;
import com.ourjourney.backend.dto.UserSearchResponse;

public interface TripMemberService {

        List<TripMemberResponse> getMembers(
                Long tripId,
                String currentUserEmail
        );

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

        List<UserSearchResponse> searchAvailableUsers(
                Long tripId,
                String query,
                String currentUserEmail
        );
}
