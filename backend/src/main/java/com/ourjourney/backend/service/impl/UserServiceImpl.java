package com.ourjourney.backend.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ourjourney.backend.dto.ChangePasswordRequest;
import com.ourjourney.backend.dto.LoginRequest;
import com.ourjourney.backend.dto.LoginResponse;
import com.ourjourney.backend.dto.RegisterRequest;
import com.ourjourney.backend.dto.UserProfileResponse;
import com.ourjourney.backend.dto.UserProfileUpdateRequest;
import com.ourjourney.backend.dto.UserResponse;
import com.ourjourney.backend.entity.User;
import com.ourjourney.backend.exception.BadRequestException;
import com.ourjourney.backend.exception.ResourceNotFoundException;
import com.ourjourney.backend.repository.UserRepository;
import com.ourjourney.backend.service.JwtService;
import com.ourjourney.backend.service.SupabaseStorageService;
import com.ourjourney.backend.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SupabaseStorageService supabaseStorageService;


    @Override
    public UserResponse register(RegisterRequest request) {
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        User user = User.builder()
        .name(request.getName())
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .build();

        User savedUser = userRepository.save(user);
        UserResponse response = new UserResponse();

        response.setId(savedUser.getId());
        response.setName(savedUser.getName());
        response.setEmail(savedUser.getEmail());
        response.setProfilePicture(savedUser.getProfilePicture());

        return response;
    }

    @Override
    public  LoginResponse login(LoginRequest request){
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        LoginResponse response = new LoginResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setProfilePicture(user.getProfilePicture());
        response.setToken(jwtService.generateToken(user.getEmail()));

        return response;
    }

    @Override
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        UserResponse response = new UserResponse();

        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setProfilePicture(user.getProfilePicture());

        return response;
    }

    @Override
    public UserProfileResponse updateCurrentUser(
            String currentEmail,
            UserProfileUpdateRequest request
    ) {
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found")
                );

        user.setName(request.getName());

        User savedUser = userRepository.save(user);

        return UserProfileResponse.builder()
                .id(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .profilePicture(savedUser.getProfilePicture())
                .build();
    }

    @Override
    public UserProfileResponse updateProfilePicture(
            String currentEmail,
            MultipartFile file
    ) {

        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );

        String profilePicture =
                supabaseStorageService.uploadProfilePicture(
                        user.getId(),
                        file
                );

        user.setProfilePicture(profilePicture);

        User savedUser = userRepository.save(user);

        UserProfileResponse response =
                new UserProfileResponse();

        response.setId(savedUser.getId());
        response.setName(savedUser.getName());
        response.setEmail(savedUser.getEmail());
        response.setProfilePicture(
                savedUser.getProfilePicture()
        );

        return response;
    }

    @Override
    public boolean verifyPassword(String email, String currentPassword) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return passwordEncoder.matches(currentPassword, user.getPassword());
    }

    @Override
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        if (request.getNewPassword().length() < 6) {
            throw new BadRequestException("Password must be at least 6 characters");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
