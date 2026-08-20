package com.ourjourney.backend.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ourjourney.backend.dto.StoredPhoto;
import com.ourjourney.backend.dto.TripPhotoResponse;
import com.ourjourney.backend.entity.TripMember;
import com.ourjourney.backend.entity.TripPhoto;
import com.ourjourney.backend.repository.TripPhotoRepository;
import com.ourjourney.backend.service.TripAuthorizationService;
import com.ourjourney.backend.service.TripPhotoService;
import com.ourjourney.backend.service.TripPhotoStorageService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TripPhotoServiceImpl implements TripPhotoService {

    private final TripPhotoRepository tripPhotoRepository;
    private final TripAuthorizationService tripAuthorizationService;
    private final TripPhotoStorageService tripPhotoStorageService;
    @Override
    public List<TripPhotoResponse> getTripPhotos(
            Long tripId,
            String currentUserEmail
    ) {
        tripAuthorizationService.getCurrentMember(tripId, currentUserEmail);

        return tripPhotoRepository
                .findByTripIdOrderByCreatedAtDesc(tripId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public TripPhotoResponse uploadPhoto(
            Long tripId,
            MultipartFile file,
            String caption,
            String currentUserEmail
    ) {
        TripMember currentMember = tripAuthorizationService
                .getCurrentMember(tripId, currentUserEmail);

        String normalizedCaption = caption == null ? null : caption.trim();

        if (normalizedCaption != null && normalizedCaption.length() > 250) {
            throw new IllegalArgumentException("Caption must not exceed 250 characters");
        }

        if (normalizedCaption != null && normalizedCaption.isEmpty()) {
            normalizedCaption = null;
        }

        StoredPhoto storedPhoto = tripPhotoStorageService.upload(tripId, file);

        TripPhoto photo = TripPhoto.builder()
                .trip(currentMember.getTrip())
                .uploadedBy(currentMember.getUser())
                .storagePath(storedPhoto.getStoragePath())
                .caption(normalizedCaption)
                .build();

        TripPhoto savedPhoto;

        try {
            savedPhoto = tripPhotoRepository.save(photo);

        } catch (RuntimeException exception) {
            try {
                tripPhotoStorageService.delete(storedPhoto.getStoragePath());
            } catch (RuntimeException cleanupException) {
                exception.addSuppressed(cleanupException);
            }
            throw exception;
        }

        return toResponse(savedPhoto);
    }

    private TripPhotoResponse toResponse(TripPhoto photo) {
        return TripPhotoResponse.builder()
                .id(photo.getId())
                .imageUrl(
                        tripPhotoStorageService.createSignedUrl(
                                photo.getStoragePath()
                        )
                )
                .caption(photo.getCaption())
                .uploadedById(photo.getUploadedBy().getId())
                .uploadedByName(photo.getUploadedBy().getName())
                .uploadedByProfilePicture(photo.getUploadedBy().getProfilePicture())
                .createdAt(photo.getCreatedAt())
                .build();
    }
}
