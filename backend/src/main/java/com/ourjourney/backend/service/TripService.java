package com.ourjourney.backend.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.ourjourney.backend.dto.TripRequest;
import com.ourjourney.backend.dto.TripResponse;

public interface TripService {
    
    TripResponse createTrip(TripRequest request, String currentUserEmail);
    List<TripResponse> getAllTrips(String currentUserEmail);
    TripResponse getTripById(Long id, String currentUserEmail);
    TripResponse updateTrip(Long id, TripRequest request, String currentUserEmail);
    TripResponse uploadCover(Long id, MultipartFile file, String currentUserEmail);
    void deleteCover(Long id, String currentUserEmail);
    void deleteTrip(Long id, String currentUserEmail);
}
