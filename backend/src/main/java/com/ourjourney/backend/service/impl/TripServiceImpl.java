package com.ourjourney.backend.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ourjourney.backend.dto.TripRequest;
import com.ourjourney.backend.dto.TripResponse;
import com.ourjourney.backend.entity.Trip;
import com.ourjourney.backend.entity.TripMember;
import com.ourjourney.backend.entity.TripMemberRole;
import com.ourjourney.backend.entity.User;
import com.ourjourney.backend.repository.TripRepository;
import com.ourjourney.backend.repository.TripMemberRepository;
import com.ourjourney.backend.repository.UserRepository;
import com.ourjourney.backend.service.TripService;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;
    private final UserRepository userRepository;

    @Override
    public TripResponse createTrip(
            TripRequest request,
            String currentUserEmail) {

        User currentUser = userRepository
                .findByEmail(currentUserEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"));

        Trip trip = Trip.builder()
                .name(request.getName())
                .description(request.getDescription())
                .destination(request.getDestination())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .coverImage(request.getCoverImage())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Trip savedTrip = tripRepository.save(trip);

        TripMember owner = TripMember.builder()
                .trip(savedTrip)
                .user(currentUser)
                .role(TripMemberRole.OWNER)
                .joinedAt(LocalDateTime.now())
                .build();

        tripMemberRepository.save(owner);

        return mapToResponse(savedTrip);
    }

        @Override
        public List<TripResponse> getAllTrips(
                String currentUserEmail) {

        User currentUser = userRepository
                .findByEmail(currentUserEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"
                        )
                );

        List<TripMember> memberships =
                tripMemberRepository.findByUserId(
                        currentUser.getId()
                );

        return memberships
                .stream()
                .map(TripMember::getTrip)
                .map(this::mapToResponse)
                .toList();
        }

    @Override
    public TripResponse getTripById(
             Long id,
            String currentUserEmail) {

        Trip trip = tripRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Trip not found"
                        )
                );

        getCurrentMember(id, currentUserEmail);

        return mapToResponse(trip);
    }

    @Override
    public TripResponse updateTrip(
             Long id,
            TripRequest request,
            String currentUserEmail) {

        Trip trip = tripRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Trip not found"
                        )
                );

        TripMember currentMember =
                getCurrentMember(id, currentUserEmail);

        if (currentMember.getRole() != TripMemberRole.OWNER) {
            throw new IllegalArgumentException(
                    "Only the trip owner can update the trip"
            );
        }

        trip.setName(request.getName());
        trip.setDescription(request.getDescription());
        trip.setDestination(request.getDestination());
        trip.setStartDate(request.getStartDate());
        trip.setEndDate(request.getEndDate());
        trip.setCoverImage(request.getCoverImage());
        trip.setUpdatedAt(LocalDateTime.now());

        Trip updatedTrip = tripRepository.save(trip);

        return mapToResponse(updatedTrip);
    }

    @Override
    @Transactional
    public void deleteTrip(
             Long id,
            String currentUserEmail) {

        if (!tripRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Trip not found"
            );
        }

        TripMember currentMember =
                getCurrentMember(id, currentUserEmail);

        if (currentMember.getRole() != TripMemberRole.OWNER) {
            throw new IllegalArgumentException(
                    "Only the trip owner can delete the trip"
            );
        }

        tripMemberRepository.deleteByTripId(id);
        tripRepository.deleteById(id);
    }

    private TripResponse mapToResponse(Trip trip) {

        TripResponse response = new TripResponse();

        response.setId(trip.getId());
        response.setName(trip.getName());
        response.setDescription(trip.getDescription());
        response.setDestination(trip.getDestination());
        response.setStartDate(trip.getStartDate());
        response.setEndDate(trip.getEndDate());
        response.setCoverImage(trip.getCoverImage());
        response.setCreatedAt(trip.getCreatedAt());
        response.setUpdatedAt(trip.getUpdatedAt());

        return response;
    }

    private TripMember getCurrentMember(
         Long tripId,
        String currentUserEmail) {

        User currentUser = userRepository
                .findByEmail(currentUserEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"
                        )
                );

        return tripMemberRepository
                .findByTripIdAndUserId(
                        tripId,
                        currentUser.getId()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "You are not a member of this trip"
                        )
                );
    }
}