package com.ourjourney.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ourjourney.backend.dto.LoginRequest;
import com.ourjourney.backend.dto.LoginResponse;
import com.ourjourney.backend.dto.RegisterRequest;
import com.ourjourney.backend.dto.UserResponse;
import com.ourjourney.backend.entity.User;
import com.ourjourney.backend.repository.UserRepository;
import com.ourjourney.backend.service.impl.UserServiceImpl;

import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock 
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserServiceImpl userService;

    private RegisterRequest createValidRegisterRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Rosmary");
        request.setEmail("rosmary@gmail.com");
        request.setPassword("password123");
        request.setConfirmPassword("password123");
        return request;
    }

    private LoginRequest createValidLoginRequest() {
        LoginRequest request = new LoginRequest();
        request.setEmail("rosmary@gmail.com");
        request.setPassword("password123");
        return request;
    }

    @BeforeEach
    void setUp() {
    }

    //Register Tests

    @Test
    void shouldRegisterUserSuccessfully() {
        RegisterRequest request = createValidRegisterRequest();

        when(userRepository.existsByEmail(request.getEmail()))
                .thenReturn(false);

        when(passwordEncoder.encode(request.getPassword()))
                .thenReturn("encodedPassword");

        User savedUser = User.builder()
                .id(1L)
                .name(request.getName())
                .email(request.getEmail())
                .password("encodedPassword")
                .build();

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        UserResponse response = userService.register(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Rosmary", response.getName());
        assertEquals("rosmary@gmail.com", response.getEmail());
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        RegisterRequest request = createValidRegisterRequest();

        when(userRepository.existsByEmail(request.getEmail()))
                .thenReturn(true);

         assertThrows(
                IllegalArgumentException.class,
                () -> userService.register(request)
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenPasswordsDoNotMatch() {
        RegisterRequest request = createValidRegisterRequest();
        request.setConfirmPassword("differentPassword");

        when(userRepository.existsByEmail(request.getEmail()))
                .thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.register(request)
        );

        verify(userRepository, never()).save(any(User.class));
    }

    //Login Tests

    @Test
    void shouldLoginUserSuccessfully() {
        LoginRequest request = createValidLoginRequest();

        User user = User.builder()
                .id(1L)
                .name("Rosmary")
                .email(request.getEmail())
                .password("encodedPassword")
                .build();

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(java.util.Optional.of(user));

        when(passwordEncoder.matches(request.getPassword(), user.getPassword()))
                .thenReturn(true);

        when(jwtService.generateToken(user.getEmail()))
        .thenReturn("test-jwt-token");

        LoginResponse response = userService.login(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Rosmary", response.getName());
        assertEquals("rosmary@gmail.com", response.getEmail());
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotExist() {
        LoginRequest request = createValidLoginRequest();

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(java.util.Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.login(request)
        );
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsIncorrect() {

        LoginRequest request = createValidLoginRequest();

        User user = User.builder()
                .id(1L)
                .name("Rosmary")
                .email(request.getEmail())
                .password("encodedPassword")
                .build();

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )).thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.login(request)
        );
    }
}
