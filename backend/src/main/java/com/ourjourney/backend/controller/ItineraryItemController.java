package com.ourjourney.backend.controller;

import com.ourjourney.backend.dto.ItineraryItemRequest;
import com.ourjourney.backend.dto.ItineraryItemResponse;
import com.ourjourney.backend.service.ItineraryItemService;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/trips/{tripId}/itinerary")
@RequiredArgsConstructor
public class ItineraryItemController {

    private final ItineraryItemService itineraryItemService;

    @GetMapping
    public ResponseEntity<List<ItineraryItemResponse>> getItems(@PathVariable Long tripId, Authentication authentication){
        return ResponseEntity.ok(
            itineraryItemService.getItems(tripId, authentication.getName())
        );
    }

    @PostMapping
    public ResponseEntity<ItineraryItemResponse> createItem(
        @PathVariable Long tripId,
        @Valid @RequestBody ItineraryItemRequest request,
        Authentication authentication

    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                    itineraryItemService.createItem(tripId, request, authentication.getName())
                );
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<ItineraryItemResponse> updateItem(
        @PathVariable Long tripId,
        @PathVariable Long itemId,
        @Valid @RequestBody ItineraryItemRequest request,
        Authentication authentication
    ) {
        return ResponseEntity.ok(
            itineraryItemService.updateItem(tripId, itemId, request, authentication.getName())
        );
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItem(
        @PathVariable Long tripId,
        @PathVariable Long itemId,
        Authentication authentication
    ) {
        itineraryItemService.deleteItem(tripId, itemId, authentication.getName());

        return ResponseEntity.noContent().build();
    }
}
