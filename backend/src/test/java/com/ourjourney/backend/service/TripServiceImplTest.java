package com.ourjourney.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import org.springframework.mock.web.MockMultipartFile;

import com.ourjourney.backend.dto.TripRequest;
import com.ourjourney.backend.dto.TripResponse;
import com.ourjourney.backend.dto.StoredPhoto;
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

    @Mock
    private TripCoverStorageService tripCoverStorageService;

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
    void shouldUploadTripCoverForOwner() {
        String email = "owner@example.com";
        User ownerUser = User.builder().id(1L).email(email).build();
        TripMember owner = TripMember.builder()
                .trip(trip)
                .user(ownerUser)
                .role(TripMemberRole.OWNER)
                .build();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cover.jpg",
                "image/jpeg",
                new byte[]{1, 2, 3}
        );
        String storagePath = "trips/1/cover/new.jpg";
        String signedUrl = "https://example.supabase.co/signed-cover";

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(ownerUser));
        when(tripMemberRepository.findByTripIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(owner));
        when(tripCoverStorageService.upload(1L, file))
                .thenReturn(new StoredPhoto(storagePath));
        when(tripRepository.save(trip)).thenReturn(trip);
        when(tripCoverStorageService.createSignedUrl(storagePath))
                .thenReturn(signedUrl);

        TripResponse response = tripService.uploadCover(1L, file, email);

        assertEquals(storagePath, trip.getCoverImage());
        assertEquals(signedUrl, response.getCoverImage());
        verify(tripCoverStorageService).upload(1L, file);
        verify(tripRepository).save(trip);
    }

    @Test
    void shouldReplaceAndDeletePreviousManagedCover() {
        String email = "owner@example.com";
        String previousCover = "trips/1/cover/old.jpg";
        String newCover = "trips/1/cover/new.jpg";
        trip.setCoverImage(previousCover);

        User ownerUser = User.builder().id(1L).email(email).build();
        TripMember owner = TripMember.builder()
                .trip(trip)
                .user(ownerUser)
                .role(TripMemberRole.OWNER)
                .build();
        MockMultipartFile file = new MockMultipartFile(
                "file", "cover.jpg", "image/jpeg", new byte[]{1}
        );

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(ownerUser));
        when(tripMemberRepository.findByTripIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(owner));
        when(tripCoverStorageService.upload(1L, file))
                .thenReturn(new StoredPhoto(newCover));
        when(tripRepository.save(trip)).thenReturn(trip);
        when(tripCoverStorageService.createSignedUrl(newCover))
                .thenReturn("https://example.com/new-cover");

        tripService.uploadCover(1L, file, email);

        verify(tripCoverStorageService).delete(previousCover);
    }

    @Test
    void shouldNotAllowMemberToUploadTripCover() {
        String email = "member@example.com";
        User memberUser = User.builder().id(2L).email(email).build();
        TripMember member = TripMember.builder()
                .trip(trip)
                .user(memberUser)
                .role(TripMemberRole.MEMBER)
                .build();
        MockMultipartFile file = new MockMultipartFile(
                "file", "cover.jpg", "image/jpeg", new byte[]{1}
        );

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(memberUser));
        when(tripMemberRepository.findByTripIdAndUserId(1L, 2L))
                .thenReturn(Optional.of(member));

        assertThrows(
                IllegalArgumentException.class,
                () -> tripService.uploadCover(1L, file, email)
        );

        verify(tripCoverStorageService, never()).upload(any(), any());
    }

    @Test
    void shouldDeleteTripCoverForOwner() {
        String email = "owner@example.com";
        String storagePath = "trips/1/cover/current.jpg";
        trip.setCoverImage(storagePath);

        User ownerUser = User.builder().id(1L).email(email).build();
        TripMember owner = TripMember.builder()
                .trip(trip)
                .user(ownerUser)
                .role(TripMemberRole.OWNER)
                .build();

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(ownerUser));
        when(tripMemberRepository.findByTripIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(owner));
        when(tripRepository.save(trip)).thenReturn(trip);

        tripService.deleteCover(1L, email);

        assertNull(trip.getCoverImage());
        verify(tripRepository).save(trip);
        verify(tripCoverStorageService).delete(storagePath);
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
        String coverPath = "trips/1/cover/current.jpg";
        trip.setCoverImage(coverPath);

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

        tripService.deleteTrip(
                1L,
                currentUserEmail
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
                .deleteById(1L);

        verify(tripCoverStorageService)
                .delete(coverPath);
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

        when(tripRepository.findById(999L))
                .thenReturn(Optional.empty());

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
