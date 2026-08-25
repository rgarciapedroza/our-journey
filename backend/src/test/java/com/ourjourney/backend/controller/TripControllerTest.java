package com.ourjourney.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ourjourney.backend.dto.TripRequest;
import com.ourjourney.backend.dto.TripResponse;
import com.ourjourney.backend.repository.UserRepository;
import com.ourjourney.backend.service.JwtService;
import com.ourjourney.backend.service.TripService;

@WebMvcTest(TripController.class)
@AutoConfigureMockMvc
class TripControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TripService tripService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    private TripRequest request;
    private TripResponse tripResponse;

    @BeforeEach
    void setUp() {

        
        request = new TripRequest();
        request.setName("The Canary Islands 2027");
        request.setDescription("New Year's trip");
        request.setDestination("Gran Canaria and Tenerife");
        request.setStartDate(LocalDate.of(2026, 12, 30));
        request.setEndDate(LocalDate.of(2027, 1, 2));
        tripResponse = new TripResponse();
        tripResponse.setId(1L);
        tripResponse.setName("The Canary Islands 2027");
        tripResponse.setDescription("New Year's trip");
        tripResponse.setDestination("Gran Canaria and Tenerife");
        tripResponse.setStartDate(LocalDate.of(2026, 12, 30));
        tripResponse.setEndDate(LocalDate.of(2027, 1, 2));
        tripResponse.setCoverImage("Maspalomas.jpg");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldCreateTripSuccessfully() throws Exception {

        when(tripService.createTrip(
                any(TripRequest.class),
                eq("test@example.com")
        )).thenReturn(tripResponse);

        mockMvc.perform(
                post("/api/trips")
                        .with(csrf())
                        .contentType("application/json")
                        .content(
                                objectMapper.writeValueAsString(request)
                        )
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name")
                .value("The Canary Islands 2027"))
        .andExpect(jsonPath("$.destination")
                .value("Gran Canaria and Tenerife"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldReturnAllTrips() throws Exception {

        TripResponse secondTrip = new TripResponse();
        secondTrip.setId(2L);
        secondTrip.setName("France Trip");
        secondTrip.setDescription("Trip around France");
        secondTrip.setDestination("France");

        when(tripService.getAllTrips("test@example.com"))
                .thenReturn(List.of(tripResponse, secondTrip));

        mockMvc.perform(
                get("/api/trips")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].name")
                .value("The Canary Islands 2027"))
        .andExpect(jsonPath("$[1].id").value(2))
        .andExpect(jsonPath("$[1].name")
                .value("France Trip"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldReturnTripById() throws Exception {

        when(tripService.getTripById(1L, "test@example.com"))
                .thenReturn(tripResponse);

        mockMvc.perform(
                get("/api/trips/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name")
                .value("The Canary Islands 2027"))
        .andExpect(jsonPath("$.destination")
                .value("Gran Canaria and Tenerife"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldReturnNotFoundWhenTripDoesNotExist() throws Exception {

        when(tripService.getTripById(999L, "test@example.com"))
                .thenThrow(
                        new IllegalArgumentException("Trip not found")
                );

        mockMvc.perform(
                get("/api/trips/999"))
        .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldUpdateTripSuccessfully() throws Exception {

        request.setName("Updated Trip");

        TripResponse updatedResponse = new TripResponse();
        updatedResponse.setId(1L);
        updatedResponse.setName("Updated Trip");
        updatedResponse.setDescription("New Year's trip");
        updatedResponse.setDestination("Gran Canaria and Tenerife");
        updatedResponse.setStartDate(
                LocalDate.of(2026, 12, 30)
        );
        updatedResponse.setEndDate(
                LocalDate.of(2027, 1, 2)
        );
        updatedResponse.setCoverImage("Maspalomas.jpg");

        when(tripService.updateTrip(
                eq(1L),
                any(TripRequest.class),
                eq("test@example.com")
        )).thenReturn(updatedResponse);

        mockMvc.perform(
                put("/api/trips/1")
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name")
                .value("Updated Trip"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldDeleteTripSuccessfully() throws Exception {

        doNothing()
                .when(tripService)
                .deleteTrip(1L, "test@example.com");

        mockMvc.perform(
                delete("/api/trips/1")
                .with(csrf()))
        .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldUploadTripCover() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cover.jpg",
                "image/jpeg",
                new byte[]{1, 2, 3}
        );

        when(tripService.uploadCover(
                eq(1L),
                any(MockMultipartFile.class),
                eq("test@example.com")
        )).thenReturn(tripResponse);

        mockMvc.perform(
                org.springframework.test.web.servlet.request
                        .MockMvcRequestBuilders.multipart("/api/trips/1/cover")
                        .file(file)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .with(csrf())
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldDeleteTripCover() throws Exception {
        doNothing()
                .when(tripService)
                .deleteCover(1L, "test@example.com");

        mockMvc.perform(
                delete("/api/trips/1/cover")
                        .with(csrf())
        )
        .andExpect(status().isNoContent());

        verify(tripService).deleteCover(1L, "test@example.com");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldReturnNotFoundWhenDeletingNonExistingTrip()
            throws Exception {

        doThrow(
                new IllegalArgumentException("Trip not found")
        )
        .when(tripService)
        .deleteTrip(999L, "test@example.com");

        mockMvc.perform(
                delete("/api/trips/999")
                .with(csrf()))
        .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnUnauthorizedWithoutToken() throws Exception {

        mockMvc.perform(
                get("/api/trips")
        )
        .andExpect(status().isUnauthorized());
    }
}
