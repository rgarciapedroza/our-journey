package com.ourjourney.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import com.ourjourney.backend.dto.ChangePasswordRequest;
import com.ourjourney.backend.dto.LoginRequest;
import com.ourjourney.backend.dto.LoginResponse;
import com.ourjourney.backend.dto.RegisterRequest;
import com.ourjourney.backend.dto.UserProfileResponse;
import com.ourjourney.backend.dto.UserProfileUpdateRequest;
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

    @Mock
    private SupabaseStorageService supabaseStorageService;

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

        @Test
        void shouldGetCurrentUserSuccessfully() {

        String email = "rosmary@gmail.com";

        User user = User.builder()
                .id(1L)
                .name("Rosmary")
                .email(email)
                .password("encodedPassword")
                .profilePicture("profile.jpg")
                .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        UserResponse response =
                userService.getCurrentUser(email);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Rosmary", response.getName());
        assertEquals(email, response.getEmail());
        assertEquals("profile.jpg", response.getProfilePicture());

        verify(userRepository)
                .findByEmail(email);
        }

        @Test
        void shouldThrowExceptionWhenCurrentUserDoesNotExist() {

        String email = "unknown@gmail.com";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.getCurrentUser(email)
        );

        verify(userRepository)
                .findByEmail(email);
        }

        @Test
        void shouldUpdateNameWithoutChangingProfilePicture() {
        String email = "rosmary@gmail.com";
        User user = User.builder()
                .id(1L)
                .name("Old Name")
                .email(email)
                .profilePicture("profile.jpg")
                .build();
        UserProfileUpdateRequest request = UserProfileUpdateRequest.builder()
                .name("Updated Name")
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserProfileResponse response = userService.updateCurrentUser(email, request);

        assertEquals("Updated Name", response.getName());
        assertEquals("profile.jpg", response.getProfilePicture());
        verify(userRepository).save(user);
        }

        @Test
        void shouldVerifyCurrentPassword() {
        String email = "rosmary@gmail.com";
        User user = User.builder()
                .email(email)
                .password("encodedPassword")
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword"))
                .thenReturn(true);

        assertTrue(userService.verifyPassword(email, "password123"));
        }


        @Test
        void shouldChangePasswordSuccessfully() {

        String email = "rosmary@gmail.com";

        User user = User.builder()
                .id(1L)
                .name("Rosmary")
                .email(email)
                .password("encodedOldPassword")
                .build();

        ChangePasswordRequest request =
                new ChangePasswordRequest();

        request.setCurrentPassword("oldPassword");
        request.setNewPassword("newPassword123");
        request.setConfirmNewPassword("newPassword123");

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "oldPassword",
                "encodedOldPassword"
        )).thenReturn(true);

        when(passwordEncoder.encode("newPassword123"))
                .thenReturn("encodedNewPassword");

        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        userService.changePassword(
                email,
                request
        );

        assertEquals(
                "encodedNewPassword",
                user.getPassword()
        );

        verify(passwordEncoder)
                .matches(
                        "oldPassword",
                        "encodedOldPassword"
                );

        verify(passwordEncoder)
                .encode("newPassword123");

        verify(userRepository)
                .save(user);
        }

        @Test
        void shouldNotChangePasswordWhenCurrentPasswordIsIncorrect() {

        String email = "rosmary@gmail.com";

        User user = User.builder()
                .id(1L)
                .email(email)
                .password("encodedOldPassword")
                .build();

        ChangePasswordRequest request =
                new ChangePasswordRequest();

        request.setCurrentPassword("wrongPassword");
        request.setNewPassword("newPassword123");
        request.setConfirmNewPassword("newPassword123");

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "wrongPassword",
                "encodedOldPassword"
        )).thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.changePassword(
                        email,
                        request
                )
        );

        verify(passwordEncoder, never())
                .encode(any(String.class));

        verify(userRepository, never())
                .save(any(User.class));
        }

        @Test
        void shouldNotChangePasswordWhenNewPasswordsDoNotMatch() {

        String email = "rosmary@gmail.com";

        User user = User.builder()
                .id(1L)
                .email(email)
                .password("encodedOldPassword")
                .build();

        ChangePasswordRequest request =
                new ChangePasswordRequest();

        request.setCurrentPassword("oldPassword");
        request.setNewPassword("newPassword123");
        request.setConfirmNewPassword("differentPassword");

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "oldPassword",
                "encodedOldPassword"
        )).thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.changePassword(
                        email,
                        request
                )
        );

        verify(passwordEncoder, never())
                .encode(any(String.class));

        verify(userRepository, never())
                .save(any(User.class));
        }

        @Test
        void shouldThrowExceptionWhenChangingPasswordForNonExistingUser() {

        String email = "unknown@gmail.com";

        ChangePasswordRequest request =
                new ChangePasswordRequest();

        request.setCurrentPassword("oldPassword");
        request.setNewPassword("newPassword123");
        request.setConfirmNewPassword("newPassword123");

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.changePassword(
                        email,
                        request
                )
        );

        verify(passwordEncoder, never())
                .encode(any(String.class));

        verify(userRepository, never())
                .save(any(User.class));
        }

        @Test
        void shouldUpdateProfilePictureSuccessfully() {

        User user = new User();
        user.setId(1L);
        user.setName("Rosmary");
        user.setEmail("rosmary@gmail.com");

        when(userRepository.findByEmail("rosmary@gmail.com"))
                .thenReturn(Optional.of(user));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "profile.jpg",
                "image/jpeg",
                "fake-image-content".getBytes()
        );

        String uploadedUrl =
                "https://example.supabase.co/storage/v1/object/public/profile-pictures/1/profile.jpg";

        when(supabaseStorageService.uploadProfilePicture(
                eq(1L),
                any(MultipartFile.class)
        )).thenReturn(uploadedUrl);

        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        UserProfileResponse response =
                userService.updateProfilePicture(
                        "rosmary@gmail.com",
                        file
                );

        assertEquals(1L, response.getId());
        assertEquals("Rosmary", response.getName());
        assertEquals("rosmary@gmail.com", response.getEmail());
        assertEquals(uploadedUrl, response.getProfilePicture());

        verify(supabaseStorageService)
                .uploadProfilePicture(
                        eq(1L),
                        eq(file)
                );

        verify(userRepository).save(user);
        }

        @Test
        void shouldThrowExceptionWhenUpdatingProfilePictureForUnknownUser() {

        when(userRepository.findByEmail("unknown@gmail.com"))
                .thenReturn(Optional.empty());

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "profile.jpg",
                "image/jpeg",
                "fake-image-content".getBytes()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateProfilePicture(
                        "unknown@gmail.com",
                        file
                )
        );

        verify(supabaseStorageService, never())
                .uploadProfilePicture(
                        any(Long.class),
                        any(MultipartFile.class)
                );
        }
}
