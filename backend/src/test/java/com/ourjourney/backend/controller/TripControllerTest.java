package com.ourjourney.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ourjourney.backend.BackendApplication;
import com.ourjourney.backend.dto.TripRequest;
import com.ourjourney.backend.dto.TripResponse;
import com.ourjourney.backend.service.JwtService;
import com.ourjourney.backend.service.TripService;

@SpringBootTest(
    classes = BackendApplication.class,
    properties = {
        "jwt.secret=test-secret-key-for-jwt-authentication"
    }
)
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

    private TripRequest request;
    private TripResponse tripResponse;
    private String token;

    @BeforeEach
    void setUp() {

        
        request = new TripRequest();
        request.setName("The Canary Islands 2027");
        request.setDescription("New Year's trip");
        request.setDestination("Gran Canaria and Tenerife");
        request.setStartDate(LocalDate.of(2026, 12, 30));
        request.setEndDate(LocalDate.of(2027, 1, 2));
        request.setCoverImage("Maspalomas.jpg");

        tripResponse = new TripResponse();
        tripResponse.setId(1L);
        tripResponse.setName("The Canary Islands 2027");
        tripResponse.setDescription("New Year's trip");
        tripResponse.setDestination("Gran Canaria and Tenerife");
        tripResponse.setStartDate(LocalDate.of(2026, 12, 30));
        tripResponse.setEndDate(LocalDate.of(2027, 1, 2));
        tripResponse.setCoverImage("Maspalomas.jpg");

        token = "valid-token";

        when(jwtService.isTokenValid(token))
                .thenReturn(true);

        when(jwtService.extractEmail(token))
                .thenReturn("rosmary@gmail.com");
    }

    @Test
    void shouldCreateTripSuccessfully() throws Exception {

        when(tripService.createTrip(any(TripRequest.class)))
                .thenReturn(tripResponse);

        mockMvc.perform(
                post("/api/trips")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name")
                .value("The Canary Islands 2027"))
        .andExpect(jsonPath("$.destination")
                .value("Gran Canaria and Tenerife"));
    }

    @Test
    void shouldReturnAllTrips() throws Exception {

        TripResponse secondTrip = new TripResponse();
        secondTrip.setId(2L);
        secondTrip.setName("France Trip");
        secondTrip.setDescription("Trip around France");
        secondTrip.setDestination("France");

        when(tripService.getAllTrips())
                .thenReturn(List.of(tripResponse, secondTrip));

        mockMvc.perform(
                get("/api/trips")
                        .header("Authorization", "Bearer " + token)
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
    void shouldReturnTripById() throws Exception {

        when(tripService.getTripById(1L))
                .thenReturn(tripResponse);

        mockMvc.perform(
                get("/api/trips/1")
                        .header("Authorization", "Bearer " + token)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name")
                .value("The Canary Islands 2027"))
        .andExpect(jsonPath("$.destination")
                .value("Gran Canaria and Tenerife"));
    }

    @Test
    void shouldReturnNotFoundWhenTripDoesNotExist() throws Exception {

        when(tripService.getTripById(999L))
                .thenThrow(
                        new IllegalArgumentException("Trip not found")
                );

        mockMvc.perform(
                get("/api/trips/999")
                        .header("Authorization", "Bearer " + token)
        )
        .andExpect(status().isNotFound());
    }

    @Test
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
                org.mockito.ArgumentMatchers.eq(1L),
                any(TripRequest.class)
        )).thenReturn(updatedResponse);

        mockMvc.perform(
                put("/api/trips/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name")
                .value("Updated Trip"));
    }

    @Test
    void shouldDeleteTripSuccessfully() throws Exception {

        doNothing()
                .when(tripService)
                .deleteTrip(1L);

        mockMvc.perform(
                delete("/api/trips/1")
                        .header("Authorization", "Bearer " + token)
        )
        .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistingTrip()
            throws Exception {

        doThrow(
                new IllegalArgumentException("Trip not found")
        )
        .when(tripService)
        .deleteTrip(999L);

        mockMvc.perform(
                delete("/api/trips/999")
                        .header("Authorization", "Bearer " + token)
        )
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
