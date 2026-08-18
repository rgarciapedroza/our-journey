package com.ourjourney.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ourjourney.backend.config.SecurityConfig;
import com.ourjourney.backend.dto.ChangePasswordRequest;
import com.ourjourney.backend.dto.UserProfileResponse;
import com.ourjourney.backend.dto.UserProfileUpdateRequest;
import com.ourjourney.backend.dto.VerifyPasswordRequest;
import com.ourjourney.backend.repository.UserRepository;
import com.ourjourney.backend.service.JwtService;
import com.ourjourney.backend.service.UserService;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private UserService userService;

        @MockitoBean
        private JwtService jwtService;

        @MockitoBean
        private UserRepository userRepository;

        @Test
        void shouldReturnUnauthorizedWhenUpdatingProfileWithoutToken()
                throws Exception {

                UserProfileUpdateRequest request =
                        new UserProfileUpdateRequest();

                request.setName("Updated Name");

                mockMvc.perform(
                        put("/api/users/me")
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "test@example.com")
        void shouldUpdateProfileSuccessfully()
                throws Exception {

                String email = "test@example.com";

                UserProfileUpdateRequest request =
                        new UserProfileUpdateRequest();

                request.setName("Updated Name");

                UserProfileResponse response =
                        new UserProfileResponse();

                response.setId(1L);
                response.setName("Updated Name");
                response.setEmail("updated@example.com");
                response.setProfilePicture(null);

                when(userService.updateCurrentUser(
                        eq(email),
                        any(UserProfileUpdateRequest.class)
                )).thenReturn(response);

                mockMvc.perform(
                        put("/api/users/me")
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk());

                verify(userService).updateCurrentUser(
                        eq(email),
                        any(UserProfileUpdateRequest.class)
                );
        }

        @Test
        void shouldReturnUnauthorizedWhenChangingPasswordWithoutToken()
                throws Exception {

                ChangePasswordRequest request =
                        new ChangePasswordRequest();

                request.setCurrentPassword("oldPassword");
                request.setNewPassword("newPassword123");
                request.setConfirmNewPassword("newPassword123");

                mockMvc.perform(
                        put("/api/users/me/password")
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "test@example.com")
        void shouldChangePasswordSuccessfully()
                throws Exception {

                String email = "test@example.com";

                ChangePasswordRequest request =
                        new ChangePasswordRequest();

                request.setCurrentPassword("oldPassword");
                request.setNewPassword("newPassword123");
                request.setConfirmNewPassword("newPassword123");

                doNothing().when(userService)
                        .changePassword(
                                eq(email),
                                any(ChangePasswordRequest.class)
                        );

                mockMvc.perform(
                        put("/api/users/me/password")
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isNoContent());

                verify(userService).changePassword(
                        eq(email),
                        any(ChangePasswordRequest.class)
                );
        }

        @Test
        @WithMockUser(username = "test@example.com")
        void shouldReturnBadRequestWhenProfileDataIsInvalid()
                throws Exception {

                UserProfileUpdateRequest request =
                        new UserProfileUpdateRequest();

                request.setName("");

                mockMvc.perform(
                        put("/api/users/me")
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(username = "test@example.com")
        void shouldReturnBadRequestWhenPasswordDataIsInvalid()
                throws Exception {

                ChangePasswordRequest request =
                        new ChangePasswordRequest();

                request.setCurrentPassword("");
                request.setNewPassword("");
                request.setConfirmNewPassword("");

                mockMvc.perform(
                        put("/api/users/me/password")
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(username = "test@example.com")
        void shouldVerifyCurrentPasswordSuccessfully() throws Exception {
                VerifyPasswordRequest request =
                        new VerifyPasswordRequest("oldPassword");

                when(userService.verifyPassword(
                        "test@example.com",
                        "oldPassword"
                )).thenReturn(true);

                mockMvc.perform(
                        post("/api/users/verify-password")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNoContent());

                verify(userService).verifyPassword(
                        "test@example.com",
                        "oldPassword"
                );
        }

        @Test
        @WithMockUser(username = "test@example.com")
        void shouldReturnUnauthorizedWhenCurrentPasswordIsIncorrect() throws Exception {
                VerifyPasswordRequest request =
                        new VerifyPasswordRequest("wrongPassword");

                when(userService.verifyPassword(
                        "test@example.com",
                        "wrongPassword"
                )).thenReturn(false);

                mockMvc.perform(
                        post("/api/users/verify-password")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "test@example.com")
        void shouldReturnBadRequestWhenPasswordVerificationIsBlank() throws Exception {
                VerifyPasswordRequest request = new VerifyPasswordRequest("");

                mockMvc.perform(
                        post("/api/users/verify-password")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
        }

        
        @Test
        void shouldReturnUnauthorizedWhenUploadingProfilePictureWithoutToken()
                throws Exception {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "profile.png",
                        "image/png",
                        "fake-image".getBytes()
                );

        mockMvc.perform(
                multipart("/api/users/me/profile-picture")
                        .file(file)
                        .with(request -> {
                                request.setMethod("PUT");
                                return request;
                        })
        )
        .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "test@example.com")
        void shouldUpdateProfilePictureSuccessfully()
                throws Exception {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "profile.png",
                        "image/png",
                        "fake-image".getBytes()
                );

        UserProfileResponse response =
                new UserProfileResponse();

        response.setId(1L);
        response.setName("Test User");
        response.setEmail("test@example.com");
        response.setProfilePicture(
                "https://test.supabase.co/profile.png"
        );

        when(userService.updateProfilePicture(
                eq("test@example.com"),
                any(MultipartFile.class)
        )).thenReturn(response);

        mockMvc.perform(
                multipart("/api/users/me/profile-picture")
                        .file(file)
                        .with(request -> {
                                request.setMethod("PUT");
                                return request;
                        })
        )
        .andExpect(status().isOk());

        verify(userService).updateProfilePicture(
                eq("test@example.com"),
                any(MultipartFile.class)
        );
        }
}
