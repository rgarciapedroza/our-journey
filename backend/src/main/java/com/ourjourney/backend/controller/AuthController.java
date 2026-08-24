package com.ourjourney.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ourjourney.backend.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.ourjourney.backend.dto.RegisterRequest;
import com.ourjourney.backend.dto.UserResponse;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor

public class AuthController {
    
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register( @Valid @RequestBody RegisterRequest request) {
        
        UserResponse response = userService.register(request);

        return ResponseEntity
                 .status(HttpStatus.CREATED)
                 .body(response);
    }
}
