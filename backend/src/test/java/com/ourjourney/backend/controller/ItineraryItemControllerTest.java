package com.ourjourney.backend.controller;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ourjourney.backend.config.SecurityConfig;
import com.ourjourney.backend.dto.ItineraryItemResponse;
import com.ourjourney.backend.exception.BadRequestException;
import com.ourjourney.backend.exception.ResourceNotFoundException;
import com.ourjourney.backend.repository.UserRepository;
import com.ourjourney.backend.service.ItineraryItemService;
import com.ourjourney.backend.service.JwtService;

@WebMvcTest(ItineraryItemController.class)
@Import(SecurityConfig.class)
class ItineraryItemControllerTest {

    private static final Long TRIP_ID = 10L;
    private static final Long ITEM_ID = 5L;
    private static final String USER_EMAIL = "member@example.com";
    private static final String VALID_REQUEST = """
            {
              "activityDate": "2026-09-14",
              "startTime": "10:00",
              "endTime": "12:00",
              "title": "Visit the Colosseum",
              "description": "Guided visit",
              "place": "Colosseum"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItineraryItemService itineraryItemService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void shouldRejectItineraryWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/trips/{tripId}/itinerary", TRIP_ID))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldReturnItineraryForAuthenticatedMember() throws Exception {
        when(itineraryItemService.getItems(TRIP_ID, USER_EMAIL))
                .thenReturn(List.of(response()));

        mockMvc.perform(get("/api/trips/{tripId}/itinerary", TRIP_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(ITEM_ID))
                .andExpect(jsonPath("$[0].tripId").value(TRIP_ID))
                .andExpect(jsonPath("$[0].createdByName").value("Rosmary"))
                .andExpect(jsonPath("$[0].activityDate").value("2026-09-14"))
                .andExpect(jsonPath("$[0].startTime").value("10:00:00"))
                .andExpect(jsonPath("$[0].title").value("Visit the Colosseum"));

        verify(itineraryItemService).getItems(TRIP_ID, USER_EMAIL);
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldCreateItineraryItemFromValidRequest() throws Exception {
        when(itineraryItemService.createItem(
                eq(TRIP_ID),
                argThat(request ->
                        "Visit the Colosseum".equals(request.getTitle())
                                && LocalTime.of(10, 0).equals(request.getStartTime())
                ),
                eq(USER_EMAIL)
        )).thenReturn(response());

        mockMvc.perform(post("/api/trips/{tripId}/itinerary", TRIP_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(ITEM_ID))
                .andExpect(jsonPath("$.title").value("Visit the Colosseum"));
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldRejectInvalidCreateRequest() throws Exception {
        String invalidRequest = """
                {
                  "activityDate": "2026-09-14",
                  "startTime": "10:00",
                  "title": ""
                }
                """;

        mockMvc.perform(post("/api/trips/{tripId}/itinerary", TRIP_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());

        verify(itineraryItemService, never()).createItem(
                eq(TRIP_ID),
                org.mockito.ArgumentMatchers.any(),
                eq(USER_EMAIL)
        );
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldUpdateItineraryItemFromValidRequest() throws Exception {
        when(itineraryItemService.updateItem(
                eq(TRIP_ID),
                eq(ITEM_ID),
                org.mockito.ArgumentMatchers.any(),
                eq(USER_EMAIL)
        )).thenReturn(response());

        mockMvc.perform(put(
                        "/api/trips/{tripId}/itinerary/{itemId}",
                        TRIP_ID,
                        ITEM_ID
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ITEM_ID));

        verify(itineraryItemService).updateItem(
                eq(TRIP_ID),
                eq(ITEM_ID),
                argThat(request -> "Visit the Colosseum".equals(request.getTitle())),
                eq(USER_EMAIL)
        );
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldReturnBadRequestWhenItineraryTimesAreInvalid() throws Exception {
        when(itineraryItemService.updateItem(
                eq(TRIP_ID),
                eq(ITEM_ID),
                org.mockito.ArgumentMatchers.any(),
                eq(USER_EMAIL)
        )).thenThrow(new BadRequestException(
                "End time must be after start time"
        ));

        mockMvc.perform(put(
                        "/api/trips/{tripId}/itinerary/{itemId}",
                        TRIP_ID,
                        ITEM_ID
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("End time must be after start time"));
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldDeleteItineraryItem() throws Exception {
        mockMvc.perform(delete(
                        "/api/trips/{tripId}/itinerary/{itemId}",
                        TRIP_ID,
                        ITEM_ID
                ))
                .andExpect(status().isNoContent());

        verify(itineraryItemService).deleteItem(
                TRIP_ID,
                ITEM_ID,
                USER_EMAIL
        );
    }

    @Test
    @WithMockUser(username = USER_EMAIL)
    void shouldReturnNotFoundWhenItineraryItemDoesNotExist() throws Exception {
        doThrow(new ResourceNotFoundException("Itinerary item not found"))
                .when(itineraryItemService)
                .deleteItem(TRIP_ID, ITEM_ID, USER_EMAIL);

        mockMvc.perform(delete(
                        "/api/trips/{tripId}/itinerary/{itemId}",
                        TRIP_ID,
                        ITEM_ID
                ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Itinerary item not found"));
    }

    private ItineraryItemResponse response() {
        return ItineraryItemResponse.builder()
                .id(ITEM_ID)
                .tripId(TRIP_ID)
                .createdById(2L)
                .createdByName("Rosmary")
                .createdByProfilePicture("https://example.com/profile.jpg")
                .activityDate(LocalDate.of(2026, 9, 14))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(12, 0))
                .title("Visit the Colosseum")
                .description("Guided visit")
                .place("Colosseum")
                .createdAt(Instant.parse("2026-08-24T09:00:00Z"))
                .updatedAt(Instant.parse("2026-08-24T09:00:00Z"))
                .build();
    }
}
