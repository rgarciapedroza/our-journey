package com.ourjourney.backend.service;

import java.util.List;

import com.ourjourney.backend.dto.TripRequest;
import com.ourjourney.backend.dto.TripResponse;

public interface TripService {
    
    TripResponse createTrip(TripRequest request, String currentUserEmail);
    List<TripResponse> getAllTrips();
    TripResponse getTripById(Long id);
    TripResponse updateTrip(Long id, TripRequest request);
    void deleteTrip(Long id);
}
