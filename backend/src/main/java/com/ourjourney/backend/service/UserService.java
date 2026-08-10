package com.ourjourney.backend.service;

import com.ourjourney.backend.dto.LoginRequest;
import com.ourjourney.backend.dto.RegisterRequest;
import com.ourjourney.backend.dto.UserResponse;

public interface UserService {
    UserResponse register(RegisterRequest request);
    UserResponse login(LoginRequest request);
}
