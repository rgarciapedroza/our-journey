package com.ourjourney.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ourjourney.backend.dto.ChangePasswordRequest;
import com.ourjourney.backend.dto.UserProfileResponse;
import com.ourjourney.backend.dto.UserProfileUpdateRequest;
import com.ourjourney.backend.service.JwtService;
import com.ourjourney.backend.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "jwt.secret=test-secret-key-for-jwt-authentication"
})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Test
    void shouldReturnUnauthorizedWhenUpdatingProfileWithoutToken()
            throws Exception {

        UserProfileUpdateRequest request =
                new UserProfileUpdateRequest();

        request.setName("Updated Name");
        request.setEmail("updated@example.com");

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
    void shouldUpdateProfileSuccessfully()
            throws Exception {

        String email = "test@example.com";
        String token = jwtService.generateToken(email);

        UserProfileUpdateRequest request =
                new UserProfileUpdateRequest();

        request.setName("Updated Name");
        request.setEmail("updated@example.com");

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
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
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
    void shouldChangePasswordSuccessfully()
            throws Exception {

        String email = "test@example.com";
        String token = jwtService.generateToken(email);

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
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
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
    void shouldReturnBadRequestWhenProfileDataIsInvalid()
            throws Exception {

        UserProfileUpdateRequest request =
                new UserProfileUpdateRequest();

        request.setName("");
        request.setEmail("invalid-email");

        String email = "test@example.com";
        String token = jwtService.generateToken(email);

        mockMvc.perform(
                put("/api/users/me")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
                        .contentType("application/json")
                        .content(
                                objectMapper.writeValueAsString(request)
                        )
        )
        .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenPasswordDataIsInvalid()
            throws Exception {

        ChangePasswordRequest request =
                new ChangePasswordRequest();

        request.setCurrentPassword("");
        request.setNewPassword("");
        request.setConfirmNewPassword("");

        String email = "test@example.com";
        String token = jwtService.generateToken(email);

        mockMvc.perform(
                put("/api/users/me/password")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
                        .contentType("application/json")
                        .content(
                                objectMapper.writeValueAsString(request)
                        )
        )
        .andExpect(status().isBadRequest());
    }
}