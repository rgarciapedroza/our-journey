package com.ourjourney.backend.service;

import org.springframework.web.multipart.MultipartFile;

import com.ourjourney.backend.dto.ChangePasswordRequest;
import com.ourjourney.backend.dto.LoginRequest;
import com.ourjourney.backend.dto.LoginResponse;
import com.ourjourney.backend.dto.RegisterRequest;
import com.ourjourney.backend.dto.UserProfileResponse;
import com.ourjourney.backend.dto.UserProfileUpdateRequest;
import com.ourjourney.backend.dto.UserResponse;

public interface UserService {
    UserResponse register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
    UserResponse getCurrentUser(String Email);
    
    UserProfileResponse updateCurrentUser(
        String currentEmail,
        UserProfileUpdateRequest request
    );

    void changePassword(
        String currentEmail,
        ChangePasswordRequest request
    );

    UserProfileResponse updateProfilePicture(
        String currentEmail,
        MultipartFile file
    );
}
