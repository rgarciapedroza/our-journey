package com.ourjourney.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.ourjourney.backend.dto.AddTripMemberRequest;
import com.ourjourney.backend.dto.TripMemberResponse;
import com.ourjourney.backend.dto.UserSearchResponse;
import com.ourjourney.backend.service.TripMemberService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/trips/{tripId}/members")
@RequiredArgsConstructor
public class TripMemberController {

    private final TripMemberService tripMemberService;

    @GetMapping
    public ResponseEntity<List<TripMemberResponse>> getMembers(
            @PathVariable Long tripId,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                tripMemberService.getMembers(
                        tripId,
                        authentication.getName()
                )
        );
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserSearchResponse>> searchAvailableUsers(
            @PathVariable Long tripId,
            @RequestParam String query,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                tripMemberService.searchAvailableUsers(
                        tripId,
                        query,
                        authentication.getName()
                )
        );
    }

    @PostMapping
    public ResponseEntity<TripMemberResponse> addMember(
            @PathVariable Long tripId,
            @Valid @RequestBody AddTripMemberRequest request,
            Authentication authentication) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                    tripMemberService.addMember(
                        tripId,
                        request,
                        authentication.getName()
                    )
                );
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long tripId,
            @PathVariable Long userId,
            Authentication authentication) {

        tripMemberService.removeMember(
                tripId,
                userId,
                authentication.getName()
        );

        return ResponseEntity.noContent().build();
    }
}
