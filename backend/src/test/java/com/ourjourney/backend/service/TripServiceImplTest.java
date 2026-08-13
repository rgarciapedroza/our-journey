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
import com.ourjourney.backend.entity.TripMemberRole;
import com.ourjourney.backend.entity.User;
import com.ourjourney.backend.repository.TripMemberRepository;
import com.ourjourney.backend.repository.TripRepository;
import com.ourjourney.backend.repository.UserRepository;
import com.ourjourney.backend.service.impl.TripServiceImpl;

@ExtendWith(MockitoExtension.class)
class TripServiceImplTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TripMemberRepository tripMemberRepository;

    @InjectMocks
    private TripServiceImpl tripService;

    private Trip trip;
    private TripRequest request;

    @BeforeEach
    void setUp() {

        trip = Trip.builder()
                .id(1L)
                .name("The Canary Islands 2027")
                .description("New Year's trip")
                .destination("Gran Canaria and Tenerife")
                .startDate(LocalDate.of(2026, 12, 30))
                .endDate(LocalDate.of(2027, 1, 2))
                .coverImage("Maspalomas.jpg")
                .build();

        request = new TripRequest();

        request.setName("The Canary Islands 2027");
        request.setDescription("New Year's trip");
        request.setDestination("Gran Canaria and Tenerife");
        request.setStartDate(
                LocalDate.of(2026, 12, 30)
        );
        request.setEndDate(
                LocalDate.of(2027, 1, 2)
        );
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

        assertEquals(
                1L,
                response.getId()
        );

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

        String currentUserEmail = "test@example.com";

        User user = User.builder()
                .id(1L)
                .name("Rosmary")
                .email(currentUserEmail)
                .build();

        Trip secondTrip = Trip.builder()
                .id(2L)
                .name("France Trip")
                .description("Trip around France")
                .destination("France")
                .startDate(LocalDate.of(2026, 7, 1))
                .endDate(LocalDate.of(2026, 7, 10))
                .build();

        TripMember firstMembership = TripMember.builder()
                .id(1L)
                .trip(trip)
                .user(user)
                .role(TripMemberRole.OWNER)
                .build();

        TripMember secondMembership = TripMember.builder()
                .id(2L)
                .trip(secondTrip)
                .user(user)
                .role(TripMemberRole.MEMBER)
                .build();

        when(userRepository.findByEmail(currentUserEmail))
                .thenReturn(Optional.of(user));

        when(tripMemberRepository.findByUserId(user.getId()))
                .thenReturn(
                        List.of(
                                firstMembership,
                                secondMembership
                        )
                );

        List<TripResponse> responses =
                tripService.getAllTrips(
                        currentUserEmail
                );

        assertEquals(
                2,
                responses.size()
        );

        assertEquals(
                "The Canary Islands 2027",
                responses.get(0).getName()
        );

        assertEquals(
                "France Trip",
                responses.get(1).getName()
        );

        verify(userRepository)
                .findByEmail(currentUserEmail);

        verify(tripMemberRepository)
                .findByUserId(user.getId());

        verify(tripRepository, never())
                .findAll();
    }

    @Test
    void shouldReturnTripById() {

        String currentUserEmail = "test@example.com";

        User user = User.builder()
                .id(1L)
                .name("Rosmary")
                .email(currentUserEmail)
                .build();

        TripMember membership = TripMember.builder()
                .id(1L)
                .trip(trip)
                .user(user)
                .role(TripMemberRole.OWNER)
                .build();

        when(tripRepository.findById(1L))
                .thenReturn(Optional.of(trip));

        when(userRepository.findByEmail(currentUserEmail))
                .thenReturn(Optional.of(user));

        when(tripMemberRepository.findByTripIdAndUserId(
                1L,
                user.getId()
        )).thenReturn(Optional.of(membership));

        TripResponse response =
                tripService.getTripById(
                        1L,
                        currentUserEmail
                );

        assertEquals(
                1L,
                response.getId()
        );

        assertEquals(
                "The Canary Islands 2027",
                response.getName()
        );

        verify(tripRepository)
                .findById(1L);

        verify(userRepository)
                .findByEmail(currentUserEmail);

        verify(tripMemberRepository)
                .findByTripIdAndUserId(
                        1L,
                        user.getId()
                );
    }

    @Test
    void shouldNotReturnTripWhenUserIsNotMember() {

        String currentUserEmail = "test@example.com";

        User user = User.builder()
                .id(2L)
                .name("Other User")
                .email(currentUserEmail)
                .build();

        when(tripRepository.findById(1L))
                .thenReturn(Optional.of(trip));

        when(userRepository.findByEmail(currentUserEmail))
                .thenReturn(Optional.of(user));

        when(tripMemberRepository.findByTripIdAndUserId(
                1L,
                user.getId()
        )).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> tripService.getTripById(
                        1L,
                        currentUserEmail
                )
        );

        verify(tripRepository)
                .findById(1L);

        verify(userRepository)
                .findByEmail(currentUserEmail);

        verify(tripMemberRepository)
                .findByTripIdAndUserId(
                        1L,
                        user.getId()
                );
    }

    @Test
    void shouldThrowExceptionWhenTripDoesNotExist() {

        when(tripRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> tripService.getTripById(
                        999L,
                        "test@example.com"
                )
        );

        verify(tripRepository)
                .findById(999L);
    }

    @Test
    void shouldUpdateTripSuccessfully() {

        String currentUserEmail = "test@example.com";

        User user = User.builder()
                .id(1L)
                .name("Rosmary")
                .email(currentUserEmail)
                .build();

        TripMember owner = TripMember.builder()
                .id(1L)
                .trip(trip)
                .user(user)
                .role(TripMemberRole.OWNER)
                .build();

        when(tripRepository.findById(1L))
                .thenReturn(Optional.of(trip));

        when(userRepository.findByEmail(currentUserEmail))
                .thenReturn(Optional.of(user));

        when(tripMemberRepository.findByTripIdAndUserId(
                1L,
                user.getId()
        )).thenReturn(Optional.of(owner));

        when(tripRepository.save(any(Trip.class)))
                .thenReturn(trip);

        request.setName("Updated Trip");

        TripResponse response =
                tripService.updateTrip(
                        1L,
                        request,
                        currentUserEmail
                );

        assertEquals(
                "Updated Trip",
                response.getName()
        );

        verify(tripRepository)
                .findById(1L);

        verify(userRepository)
                .findByEmail(currentUserEmail);

        verify(tripMemberRepository)
                .findByTripIdAndUserId(
                        1L,
                        user.getId()
                );

        verify(tripRepository)
                .save(trip);
    }

    @Test
    void shouldNotAllowMemberToUpdateTrip() {

        String currentUserEmail = "member@example.com";

        User user = User.builder()
                .id(2L)
                .name("Member")
                .email(currentUserEmail)
                .build();

        TripMember member = TripMember.builder()
                .id(2L)
                .trip(trip)
                .user(user)
                .role(TripMemberRole.MEMBER)
                .build();

        when(tripRepository.findById(1L))
                .thenReturn(Optional.of(trip));

        when(userRepository.findByEmail(currentUserEmail))
                .thenReturn(Optional.of(user));

        when(tripMemberRepository.findByTripIdAndUserId(
                1L,
                user.getId()
        )).thenReturn(Optional.of(member));

        assertThrows(
                IllegalArgumentException.class,
                () -> tripService.updateTrip(
                        1L,
                        request,
                        currentUserEmail
                )
        );

        verify(tripRepository, never())
                .save(any(Trip.class));
    }

    @Test
    void shouldDeleteTripSuccessfully() {

        String currentUserEmail = "test@example.com";

        User user = User.builder()
                .id(1L)
                .name("Rosmary")
                .email(currentUserEmail)
                .build();

        TripMember owner = TripMember.builder()
                .id(1L)
                .trip(trip)
                .user(user)
                .role(TripMemberRole.OWNER)
                .build();

        when(tripRepository.existsById(1L))
                .thenReturn(true);

        when(userRepository.findByEmail(currentUserEmail))
                .thenReturn(Optional.of(user));

        when(tripMemberRepository.findByTripIdAndUserId(
                1L,
                user.getId()
        )).thenReturn(Optional.of(owner));

        tripService.deleteTrip(
                1L,
                currentUserEmail
        );

        verify(tripRepository)
                .existsById(1L);

        verify(userRepository)
                .findByEmail(currentUserEmail);

        verify(tripMemberRepository)
                .findByTripIdAndUserId(
                        1L,
                        user.getId()
                );

        verify(tripRepository)
                .deleteById(1L);
    }

    @Test
    void shouldNotAllowMemberToDeleteTrip() {

        String currentUserEmail = "member@example.com";

        User user = User.builder()
                .id(2L)
                .name("Member")
                .email(currentUserEmail)
                .build();

        TripMember member = TripMember.builder()
                .id(2L)
                .trip(trip)
                .user(user)
                .role(TripMemberRole.MEMBER)
                .build();

        when(tripRepository.existsById(1L))
                .thenReturn(true);

        when(userRepository.findByEmail(currentUserEmail))
                .thenReturn(Optional.of(user));

        when(tripMemberRepository.findByTripIdAndUserId(
                1L,
                user.getId()
        )).thenReturn(Optional.of(member));

        assertThrows(
                IllegalArgumentException.class,
                () -> tripService.deleteTrip(
                        1L,
                        currentUserEmail
                )
        );

        verify(tripRepository, never())
                .deleteById(1L);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistingTrip() {

        when(tripRepository.existsById(999L))
                .thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> tripService.deleteTrip(
                        999L,
                        "test@example.com"
                )
        );

        verify(tripRepository, never())
                .deleteById(999L);
    }

    @Test
    void shouldThrowExceptionWhenGettingTripsForUnknownUser() {

    String currentUserEmail = "unknown@example.com";

    when(userRepository.findByEmail(currentUserEmail))
        .thenReturn(Optional.empty());

    assertThrows(
        IllegalArgumentException.class,
        () -> tripService.getAllTrips(
                currentUserEmail
        )
    );

    verify(userRepository)
        .findByEmail(currentUserEmail);

    verify(tripMemberRepository, never())
        .findByUserId(any());
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoTrips() {

    String currentUserEmail = "test@example.com";

    User user = User.builder()
        .id(1L)
        .name("Rosmary")
        .email(currentUserEmail)
        .build();

    when(userRepository.findByEmail(currentUserEmail))
        .thenReturn(Optional.of(user));

    when(tripMemberRepository.findByUserId(user.getId()))
        .thenReturn(List.of());

    List<TripResponse> responses =
        tripService.getAllTrips(
                currentUserEmail
        );

    assertEquals(
        0,
        responses.size()
    );

    verify(userRepository)
        .findByEmail(currentUserEmail);

    verify(tripMemberRepository)
        .findByUserId(user.getId());

    verify(tripRepository, never())
        .findAll();
    }
}