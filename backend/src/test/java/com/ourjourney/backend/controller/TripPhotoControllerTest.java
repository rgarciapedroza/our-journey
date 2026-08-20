package com.ourjourney.backend.controller;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ourjourney.backend.config.SecurityConfig;
import com.ourjourney.backend.dto.TripPhotoResponse;
import com.ourjourney.backend.repository.UserRepository;
import com.ourjourney.backend.service.JwtService;
import com.ourjourney.backend.service.TripPhotoService;

@WebMvcTest(TripPhotoController.class)
@Import(SecurityConfig.class)
class TripPhotoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TripPhotoService tripPhotoService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void shouldRejectPhotoListWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/trips/{tripId}/photos", 10L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "member@example.com")
    void shouldReturnTripPhotosForAuthenticatedMember() throws Exception {
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 20, 12, 0);
        TripPhotoResponse photo = TripPhotoResponse.builder()
                .id(5L)
                .imageUrl("https://example.com/signed-photo")
                .caption("Sunset in Rome")
                .uploadedById(2L)
                .uploadedByName("Rosmary")
                .uploadedByProfilePicture("https://example.com/profile.jpg")
                .createdAt(createdAt)
                .build();

        when(tripPhotoService.getTripPhotos(10L, "member@example.com"))
                .thenReturn(List.of(photo));

        mockMvc.perform(get("/api/trips/{tripId}/photos", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5L))
                .andExpect(jsonPath("$[0].imageUrl")
                        .value("https://example.com/signed-photo"))
                .andExpect(jsonPath("$[0].caption").value("Sunset in Rome"))
                .andExpect(jsonPath("$[0].uploadedById").value(2L))
                .andExpect(jsonPath("$[0].uploadedByName").value("Rosmary"))
                .andExpect(jsonPath("$[0].uploadedByProfilePicture")
                        .value("https://example.com/profile.jpg"));

        verify(tripPhotoService).getTripPhotos(
                10L,
                "member@example.com"
        );
    }

    @Test
    void shouldRejectPhotoUploadWithoutAuthentication() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "rome.jpg",
                "image/jpeg",
                "image-content".getBytes()
        );

        mockMvc.perform(
                multipart("/api/trips/{tripId}/photos", 10L)
                        .file(file)
        )
        .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "member@example.com")
    void shouldUploadPhotoForAuthenticatedMember() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "rome.jpg",
                "image/jpeg",
                "image-content".getBytes()
        );
        TripPhotoResponse response = TripPhotoResponse.builder()
                .id(6L)
                .imageUrl("https://example.com/new-signed-photo")
                .caption("First day")
                .uploadedById(2L)
                .uploadedByName("Rosmary")
                .createdAt(LocalDateTime.of(2026, 8, 20, 13, 0))
                .build();

        when(tripPhotoService.uploadPhoto(
                org.mockito.ArgumentMatchers.eq(10L),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq("First day"),
                org.mockito.ArgumentMatchers.eq("member@example.com")
        )).thenReturn(response);

        mockMvc.perform(
                multipart("/api/trips/{tripId}/photos", 10L)
                        .file(file)
                        .param("caption", "First day")
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(6L))
        .andExpect(jsonPath("$.imageUrl")
                .value("https://example.com/new-signed-photo"))
        .andExpect(jsonPath("$.caption").value("First day"));

        verify(tripPhotoService).uploadPhoto(
                org.mockito.ArgumentMatchers.eq(10L),
                argThat(uploadedFile ->
                        "rome.jpg".equals(uploadedFile.getOriginalFilename())
                                && "image/jpeg".equals(uploadedFile.getContentType())
                ),
                org.mockito.ArgumentMatchers.eq("First day"),
                org.mockito.ArgumentMatchers.eq("member@example.com")
        );
    }
}
