package com.ourjourney.backend.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ourjourney.backend.dto.TripRequest;
import com.ourjourney.backend.dto.TripResponse;
import com.ourjourney.backend.entity.Trip;
import com.ourjourney.backend.repository.TripRepository;
import com.ourjourney.backend.service.TripService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TripServiceImpl implements TripService{
    
    private final TripRepository tripRepository;

    @Override
    public TripResponse createTrip(TripRequest request){
        
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

        return mapToResponse(savedTrip);
    }

    @Override
    public List<TripResponse> getAllTrips() {
        return tripRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();

    }

    @Override
    public TripResponse getTripById(Long id) {

        Trip trip = tripRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Trip not found"));

        return mapToResponse(trip);
    }

    @Override
    public TripResponse updateTrip(Long id, TripRequest request) {

        Trip trip = tripRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Trip not found"));

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
    public void deleteTrip(Long id) {

        if (!tripRepository.existsById(id)) {
            throw new IllegalArgumentException("Trip not found");
        }

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
}
