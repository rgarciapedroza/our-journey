package com.ourjourney.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ourjourney.backend.dto.TripPhotoResponse;
import com.ourjourney.backend.service.TripPhotoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/trips/{tripId}/photos")
@RequiredArgsConstructor
public class TripPhotoController {

    private final TripPhotoService tripPhotoService;

    @GetMapping
    public ResponseEntity<List<TripPhotoResponse>> getTripPhotos(@PathVariable Long tripId, Authentication authentication) {
        return ResponseEntity.ok(
                tripPhotoService.getTripPhotos(tripId, authentication.getName()));
    }

    @PostMapping
    public ResponseEntity<TripPhotoResponse> uploadPhoto(
            @PathVariable Long tripId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "caption", required = false) String caption,
            Authentication authentication
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(tripPhotoService.uploadPhoto(tripId, file, caption, authentication.getName()));
    }

    @DeleteMapping("/{photoId}")
    public ResponseEntity<Void> deletePhoto(
        @PathVariable Long tripId,
        @PathVariable Long photoId,
        Authentication authentication
    ){
        tripPhotoService.deletePhoto(tripId, photoId, authentication.getName());
    
        return ResponseEntity.noContent().build();
    }
}
