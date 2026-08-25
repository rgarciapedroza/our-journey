package com.ourjourney.backend.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
import com.ourjourney.backend.service.TripCoverStorageService;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;
    private final UserRepository userRepository;
    private final TripCoverStorageService tripCoverStorageService;

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
        trip.setUpdatedAt(LocalDateTime.now());

        Trip updatedTrip = tripRepository.save(trip);

        return mapToResponse(updatedTrip);
    }

    @Override
    public TripResponse uploadCover(
            Long id,
            MultipartFile file,
            String currentUserEmail
    ) {
        Trip trip = findTrip(id);
        requireOwner(id, currentUserEmail);

        String previousCover = trip.getCoverImage();
        String newCover = tripCoverStorageService
                .upload(id, file)
                .getStoragePath();

        trip.setCoverImage(newCover);
        Trip updatedTrip;

        try {
            updatedTrip = tripRepository.save(trip);
        } catch (RuntimeException exception) {
            deleteManagedCoverSafely(newCover);
            throw exception;
        }

        deleteManagedCoverSafely(previousCover);
        return mapToResponse(updatedTrip);
    }

    @Override
    public void deleteCover(
            Long id,
            String currentUserEmail
    ) {
        Trip trip = findTrip(id);
        requireOwner(id, currentUserEmail);

        String previousCover = trip.getCoverImage();

        if (previousCover == null || previousCover.isBlank()) {
            return;
        }

        trip.setCoverImage(null);
        tripRepository.save(trip);
        deleteManagedCoverSafely(previousCover);
    }

    @Override
    @Transactional
    public void deleteTrip(
             Long id,
            String currentUserEmail) {

        Trip trip = findTrip(id);
        requireOwner(id, currentUserEmail);

        tripMemberRepository.deleteByTripId(id);
        tripRepository.deleteById(id);
        deleteManagedCoverSafely(trip.getCoverImage());
    }

    private TripResponse mapToResponse(Trip trip) {

        TripResponse response = new TripResponse();

        response.setId(trip.getId());
        response.setName(trip.getName());
        response.setDescription(trip.getDescription());
        response.setDestination(trip.getDestination());
        response.setStartDate(trip.getStartDate());
        response.setEndDate(trip.getEndDate());
        response.setCoverImage(resolveCoverUrl(trip.getCoverImage()));
        response.setCreatedAt(trip.getCreatedAt());
        response.setUpdatedAt(trip.getUpdatedAt());

        return response;
    }

    private Trip findTrip(Long id) {
        return tripRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Trip not found")
                );
    }

    private void requireOwner(Long tripId, String currentUserEmail) {
        TripMember currentMember = getCurrentMember(tripId, currentUserEmail);

        if (currentMember.getRole() != TripMemberRole.OWNER) {
            throw new IllegalArgumentException(
                    "Only the trip owner can update the trip cover"
            );
        }
    }

    private String resolveCoverUrl(String coverImage) {
        if (coverImage == null || coverImage.isBlank()) {
            return null;
        }

        if (!isManagedCover(coverImage)) {
            return coverImage;
        }

        return tripCoverStorageService.createSignedUrl(coverImage);
    }

    private void deleteManagedCoverSafely(String coverImage) {
        if (!isManagedCover(coverImage)) {
            return;
        }

        try {
            tripCoverStorageService.delete(coverImage);
        } catch (RuntimeException exception) {
            log.warn("Could not remove trip cover from storage: {}", coverImage, exception);
        }
    }

    private boolean isManagedCover(String coverImage) {
        return coverImage != null && coverImage.startsWith("trips/");
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
