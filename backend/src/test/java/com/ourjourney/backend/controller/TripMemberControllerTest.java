package com.ourjourney.backend.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ourjourney.backend.config.SecurityConfig;
import com.ourjourney.backend.dto.UserSearchResponse;
import com.ourjourney.backend.repository.UserRepository;
import com.ourjourney.backend.service.JwtService;
import com.ourjourney.backend.service.TripMemberService;

@WebMvcTest(TripMemberController.class)
@Import(SecurityConfig.class)
class TripMemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TripMemberService tripMemberService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void shouldRejectSearchWithoutAuthentication() throws Exception {
        mockMvc.perform(
                get("/api/trips/{tripId}/members/search", 10L)
                        .param("query", "ros")
        )
        .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "owner@example.com")
    void shouldReturnAvailableUsersWithProfileDetails() throws Exception {
        UserSearchResponse candidate = new UserSearchResponse(
                2L,
                "Rosmary Smith",
                "rosmary@example.com",
                "https://example.com/profile.jpg"
        );

        when(tripMemberService.searchAvailableUsers(
                10L,
                "ros",
                "owner@example.com"
        )).thenReturn(List.of(candidate));

        mockMvc.perform(
                get("/api/trips/{tripId}/members/search", 10L)
                        .param("query", "ros")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(2L))
        .andExpect(jsonPath("$[0].name").value("Rosmary Smith"))
        .andExpect(jsonPath("$[0].email").value("rosmary@example.com"))
        .andExpect(jsonPath("$[0].profilePicture")
                .value("https://example.com/profile.jpg"));

        verify(tripMemberService).searchAvailableUsers(
                10L,
                "ros",
                "owner@example.com"
        );
    }
}
