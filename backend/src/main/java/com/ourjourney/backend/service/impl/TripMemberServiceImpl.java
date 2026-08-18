package com.ourjourney.backend.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.ourjourney.backend.dto.AddTripMemberRequest;
import com.ourjourney.backend.dto.TripMemberResponse;
import com.ourjourney.backend.dto.UserSearchResponse;
import com.ourjourney.backend.entity.Trip;
import com.ourjourney.backend.entity.TripMember;
import com.ourjourney.backend.entity.TripMemberRole;
import com.ourjourney.backend.entity.User;
import com.ourjourney.backend.repository.TripMemberRepository;
import com.ourjourney.backend.repository.TripRepository;
import com.ourjourney.backend.repository.UserRepository;
import com.ourjourney.backend.service.TripMemberService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TripMemberServiceImpl
        implements TripMemberService {
    
    private final TripMemberRepository tripMemberRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;


    @Override
    public List<TripMemberResponse> getMembers( Long tripId) {

        if (!tripRepository.existsById(tripId)) {
            throw new IllegalArgumentException("Trip not found");
        }

        return tripMemberRepository.findByTripId(tripId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private TripMemberResponse toResponse(
        TripMember member) {

        TripMemberResponse response =
                new TripMemberResponse();

        response.setUserId(member.getUser().getId());
        response.setName(member.getUser().getName());
        response.setEmail(member.getUser().getEmail());
        response.setProfilePicture(
                member.getUser().getProfilePicture()
        );
        response.setRole(member.getRole());

        return response;
    }

    @Override
    public TripMemberResponse addMember(
             Long tripId,
            AddTripMemberRequest request,
            String currentUserEmail) {

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Trip not found"
                        )
                );

        User currentUser = userRepository
                .findByEmail(currentUserEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"
                        )
                );

        TripMember owner =
                tripMemberRepository
                        .findByTripIdAndUserId(
                                tripId,
                                currentUser.getId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "You are not a member of this trip"
                                )
                        );

        if (owner.getRole() != TripMemberRole.OWNER) {
            throw new IllegalArgumentException(
                    "Only the trip owner can add members"
            );
        }

        User userToAdd = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"
                        )
                );

        if (tripMemberRepository.existsByTripIdAndUserId(
                tripId,
                userToAdd.getId())) {

            throw new IllegalArgumentException(
                    "User is already a member"
            );
        }

        TripMember member = TripMember.builder()
                .trip(trip)
                .user(userToAdd)
                .role(TripMemberRole.MEMBER)
                .joinedAt(LocalDateTime.now())
                .build();

        return toResponse(
                tripMemberRepository.save(member)
        );
    }

     @Override
    public void removeMember(
             Long tripId,
             Long userId,
            String currentUserEmail) {

        User currentUser = userRepository
                .findByEmail(currentUserEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        TripMember currentMember =
                tripMemberRepository
                        .findByTripIdAndUserId(
                                tripId,
                                currentUser.getId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "You are not a member of this trip"
                                ));

        if (currentMember.getRole() != TripMemberRole.OWNER) {
            throw new IllegalArgumentException(
                    "Only the trip owner can remove members"
            );
        }

        TripMember memberToRemove =
                tripMemberRepository
                        .findByTripIdAndUserId(tripId, userId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User is not a member of this trip"
                                ));

        if (memberToRemove.getRole() == TripMemberRole.OWNER) {
            throw new IllegalArgumentException(
                    "The trip owner cannot be removed"
            );
        }

                tripMemberRepository.delete(memberToRemove);
        }

        @Override
        public List<UserSearchResponse> searchAvailableUsers(
                Long tripId,
                String query,
                String currentUserEmail
        ) {
        String normalizedQuery = query == null ? "" : query.trim();

        if (normalizedQuery.length() < 2) {
                return List.of();
        }

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found")
                );

        TripMember currentMember = tripMemberRepository
                .findByTripIdAndUserId(tripId, currentUser.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "You are not a member of this trip"
                        )
                );

        if (currentMember.getRole() != TripMemberRole.OWNER) {
                throw new IllegalArgumentException(
                        "Only the trip owner can search users"
                );
        }

        return userRepository.searchAvailableTripMembers(
                        tripId,
                        normalizedQuery,
                        PageRequest.of(0, 10)
                )
                .stream()
                .map(user -> new UserSearchResponse(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getProfilePicture()
                ))
                .toList();
        }
}
