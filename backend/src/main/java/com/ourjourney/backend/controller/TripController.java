package com.ourjourney.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ourjourney.backend.dto.TripRequest;
import com.ourjourney.backend.dto.TripResponse;
import com.ourjourney.backend.service.TripService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {
    
    private final TripService tripService;

    @PostMapping
    public ResponseEntity<TripResponse> createTrip(@Valid @RequestBody TripRequest request, Authentication authentication) {
        return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(tripService.createTrip(request, authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<List<TripResponse>> getAllTrips(
            Authentication authentication) {
        return ResponseEntity.ok(
                tripService.getAllTrips(
                        authentication.getName()
                )
        );
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TripResponse> getTripById( @PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(
                tripService.getTripById(id, authentication.getName())
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<TripResponse> updateTrip(
            @PathVariable Long id,
            @Valid @RequestBody TripRequest request,
            Authentication authentication
        ) {

        return ResponseEntity.ok(
                tripService.updateTrip(id, request, authentication.getName())
        );
    }

    @PutMapping(
            value = "/{id}/cover",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<TripResponse> uploadCover(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                tripService.uploadCover(
                        id,
                        file,
                        authentication.getName()
                )
        );
    }

    @DeleteMapping("/{id}/cover")
    public ResponseEntity<Void> deleteCover(
            @PathVariable Long id,
            Authentication authentication
    ) {
        tripService.deleteCover(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrip(
            @PathVariable Long id,
            Authentication authentication
        ) {

        tripService.deleteTrip(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
