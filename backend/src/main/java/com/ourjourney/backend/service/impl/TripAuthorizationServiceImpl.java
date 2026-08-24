package com.ourjourney.backend.service.impl;

import org.springframework.stereotype.Service;

import com.ourjourney.backend.entity.TripMember;
import com.ourjourney.backend.entity.TripMemberRole;
import com.ourjourney.backend.entity.User;
import com.ourjourney.backend.repository.TripMemberRepository;
import com.ourjourney.backend.repository.UserRepository;
import com.ourjourney.backend.service.TripAuthorizationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TripAuthorizationServiceImpl implements TripAuthorizationService {

    private final UserRepository userRepository;
    private final TripMemberRepository tripMemberRepository;

    @Override
    public TripMember getCurrentMember(
            Long tripId,
            String email
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                    new IllegalArgumentException("User not found")
                );

        return tripMemberRepository
                .findByTripIdAndUserId(
                        tripId,
                        user.getId()
                )
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "You are not a member of this trip"
                    )
                );
    }

    public void requireMember(
            Long tripId,
            String email
    ) {
        getCurrentMember(tripId, email);
    }

    public void requireOwner(
            Long tripId,
            String email
    ) {
        TripMember member =
                getCurrentMember(tripId, email);

        if (member.getRole() != TripMemberRole.OWNER) {
            throw new IllegalArgumentException(
                "Only the trip owner can perform this action"
            );
        }
    }
}
