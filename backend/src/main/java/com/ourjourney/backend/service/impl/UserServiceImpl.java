package com.ourjourney.backend.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ourjourney.backend.dto.LoginRequest;
import com.ourjourney.backend.dto.LoginRequest;
import com.ourjourney.backend.dto.LoginRequest;
import com.ourjourney.backend.dto.RegisterRequest;
import com.ourjourney.backend.dto.UserResponse;
import com.ourjourney.backend.entity.User;
import com.ourjourney.backend.repository.UserRepository;
import com.ourjourney.backend.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    

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
    public  UserResponse login(LoginRequest request){
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setProfilePicture(user.getProfilePicture());

        return response;
    }
}
