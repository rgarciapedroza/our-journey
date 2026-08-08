package com.ourjourney.backend.service;

import com.ourjourney.backend.dto.RegisterRequest;
import com.ourjourney.backend.dto.UserResponse;

public interface UserService {
    UserResponse register(RegisterRequest request);
}
