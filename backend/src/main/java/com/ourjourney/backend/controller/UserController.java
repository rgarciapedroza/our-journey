package com.ourjourney.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ourjourney.backend.dto.ChangePasswordRequest;
import com.ourjourney.backend.dto.UserProfileResponse;
import com.ourjourney.backend.dto.UserProfileUpdateRequest;
import com.ourjourney.backend.dto.VerifyPasswordRequest;
import com.ourjourney.backend.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateCurrentUser(
            Authentication authentication,
            @Valid @RequestBody UserProfileUpdateRequest request
    ) {
        return ResponseEntity.ok(
                userService.updateCurrentUser(authentication.getName(), request)
        );
    }

    @PutMapping("/me/profile-picture")
    public ResponseEntity<UserProfileResponse> updateProfilePicture(
            Authentication authentication,
            @RequestPart("file") MultipartFile file
    ) {
        return ResponseEntity.ok(
                userService.updateProfilePicture(authentication.getName(), file)
        );
    }

    @PostMapping("/verify-password")
    public ResponseEntity<Void> verifyPassword(
            Authentication authentication,
            @Valid @RequestBody VerifyPasswordRequest request
    ) {
        boolean isValid = userService.verifyPassword(
                authentication.getName(),
                request.getCurrentPassword()
        );

        if (!isValid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(authentication.getName(), request);
        return ResponseEntity.noContent().build();
    }
}