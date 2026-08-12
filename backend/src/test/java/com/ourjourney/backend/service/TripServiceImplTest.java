package com.ourjourney.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ourjourney.backend.dto.TripRequest;
import com.ourjourney.backend.dto.TripResponse;
import com.ourjourney.backend.entity.Trip;
import com.ourjourney.backend.entity.TripMember;
import com.ourjourney.backend.entity.User;
import com.ourjourney.backend.repository.TripMemberRepository;
import com.ourjourney.backend.repository.TripRepository;
import com.ourjourney.backend.repository.UserRepository;
import com.ourjourney.backend.service.impl.TripServiceImpl;

@ExtendWith(MockitoExtension.class)
class TripServiceImplTest {
    
    @Mock
    private TripRepository tripRepository;

    @InjectMocks
    private TripServiceImpl tripService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TripMemberRepository tripMemberRepository;


    private Trip trip;
    private TripRequest request;

    @BeforeEach
    void setUp() {

        trip = Trip.builder()
                .id(1L)
                .name("The Canary Islands 2027")
                .description("New Years's trip")
                .destination("Gran Canaria and Tenerife")
                .startDate(LocalDate.of(2026, 12, 30))
                .endDate(LocalDate.of(2027, 1, 2))
                .coverImage("Maspalomas.jpg")
                .build();

        request = new TripRequest();
        request.setName("The Canary Islands 2027");
        request.setDescription("New Years's trip");
        request.setDestination("Gran Canaria and Tenerife");
        request.setStartDate(LocalDate.of(2026, 12, 30));
        request.setEndDate(LocalDate.of(2027, 1, 2));
        request.setCoverImage("Maspalomas.jpg");
    }

    @Test
    void shouldCreateTripSuccessfully() {

        String currentUserEmail = "test@example.com";

        User user = User.builder()
                .id(1L)
                .name("Rosmary")
                .email(currentUserEmail)
                .build();

        when(userRepository.findByEmail(currentUserEmail))
                .thenReturn(Optional.of(user));

        when(tripRepository.save(any(Trip.class)))
                .thenReturn(trip);

        when(tripMemberRepository.save(any(TripMember.class)))
                .thenReturn(new TripMember());

        TripResponse response =
                tripService.createTrip(
                        request,
                        currentUserEmail
                );

        assertEquals(1L, response.getId());
        assertEquals(
                "The Canary Islands 2027",
                response.getName()
        );
        assertEquals(
                "Gran Canaria and Tenerife",
                response.getDestination()
        );

        verify(userRepository)
                .findByEmail(currentUserEmail);

        verify(tripRepository)
                .save(any(Trip.class));

        verify(tripMemberRepository)
                .save(any(TripMember.class));
    }
    @Test
    void shouldReturnAllTrips() {

        Trip secondTrip = Trip.builder()
                .id(2L)
                .name("France Trip")
                .description("Trip around France")
                .destination("France")
                .startDate(LocalDate.of(2026, 7, 1))
                .endDate(LocalDate.of(2026, 7, 10))
                .build();

        when(tripRepository.findAll())
                .thenReturn(List.of(trip, secondTrip));

        List<TripResponse> responses = tripService.getAllTrips();

        assertEquals(2, responses.size());
        assertEquals("The Canary Islands 2027", responses.get(0).getName());
        assertEquals("France Trip", responses.get(1).getName());

        verify(tripRepository).findAll();
    }

    @Test
    void shouldReturnTripById() {

        when(tripRepository.findById(1L))
                .thenReturn(Optional.of(trip));

        TripResponse response = tripService.getTripById(1L);

        assertEquals(1L, response.getId());
        assertEquals("The Canary Islands 2027", response.getName());

        verify(tripRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenTripDoesNotExist() {

        when(tripRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> tripService.getTripById(999L)
        );

        verify(tripRepository).findById(999L);
    }

    @Test
    void shouldUpdateTripSuccessfully() {

        when(tripRepository.findById(1L))
                .thenReturn(Optional.of(trip));

        when(tripRepository.save(any(Trip.class)))
                .thenReturn(trip);

        request.setName("Updated Trip");

        TripResponse response = tripService.updateTrip(1L, request);

        assertEquals("Updated Trip", response.getName());

        verify(tripRepository).findById(1L);
        verify(tripRepository).save(trip);
    }

    @Test
    void shouldDeleteTripSuccessfully() {

        when(tripRepository.existsById(1L))
                .thenReturn(true);

        tripService.deleteTrip(1L);

        verify(tripRepository).existsById(1L);
        verify(tripRepository).deleteById(1L);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistingTrip() {

        when(tripRepository.existsById(999L))
                .thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> tripService.deleteTrip(999L)
        );

        verify(tripRepository, never()).deleteById(999L);
    }
}
