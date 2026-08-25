package com.ourjourney.backend.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.ourjourney.backend.dto.TripPhotoResponse;

public interface TripPhotoService {

    List<TripPhotoResponse> getTripPhotos(
            Long tripId,
            String currentUserEmail
    );

    TripPhotoResponse uploadPhoto(
            Long tripId,
            MultipartFile file,
            String caption,
            String currentUserEmail
    );

    void deletePhoto(
        Long tripId,
        Long photoId,
        String currentUserEmail
    );
}
