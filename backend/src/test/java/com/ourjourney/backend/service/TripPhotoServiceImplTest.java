package com.ourjourney.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import com.ourjourney.backend.dto.StoredPhoto;
import com.ourjourney.backend.dto.TripPhotoResponse;
import com.ourjourney.backend.entity.Trip;
import com.ourjourney.backend.entity.TripMember;
import com.ourjourney.backend.entity.TripMemberRole;
import com.ourjourney.backend.entity.TripPhoto;
import com.ourjourney.backend.entity.User;
import com.ourjourney.backend.repository.TripPhotoRepository;
import com.ourjourney.backend.service.impl.TripPhotoServiceImpl;

@ExtendWith(MockitoExtension.class)
class TripPhotoServiceImplTest {

    private static final Long TRIP_ID = 1L;
    private static final String USER_EMAIL = "member@example.com";
    private static final String STORAGE_PATH = "trips/1/photo.jpg";
    private static final String SIGNED_URL = "https://storage.example/signed-photo";

    @Mock
    private TripPhotoRepository tripPhotoRepository;

    @Mock
    private TripAuthorizationService tripAuthorizationService;

    @Mock
    private TripPhotoStorageService tripPhotoStorageService;

    @Mock
    private MultipartFile file;

    @InjectMocks
    private TripPhotoServiceImpl tripPhotoService;

    private Trip trip;
    private User user;
    private TripMember membership;

    @BeforeEach
    void setUp() {
        trip = Trip.builder().id(TRIP_ID).name("Rome").build();
        user = User.builder()
                .id(2L)
                .name("Rosmary")
                .email(USER_EMAIL)
                .profilePicture("https://example.com/profile.jpg")
                .build();
        membership = TripMember.builder()
                .id(3L)
                .trip(trip)
                .user(user)
                .role(TripMemberRole.MEMBER)
                .build();
    }

    @Test
    void shouldReturnTripPhotosForMember() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 20, 10, 30);
        TripPhoto photo = savedPhoto("Sunset", createdAt);

        when(tripAuthorizationService.getCurrentMember(TRIP_ID, USER_EMAIL))
                .thenReturn(membership);
        when(tripPhotoRepository.findByTripIdOrderByCreatedAtDesc(TRIP_ID))
                .thenReturn(List.of(photo));
        when(tripPhotoStorageService.createSignedUrl(STORAGE_PATH))
                .thenReturn(SIGNED_URL);

        List<TripPhotoResponse> responses =
                tripPhotoService.getTripPhotos(TRIP_ID, USER_EMAIL);

        assertEquals(1, responses.size());
        TripPhotoResponse response = responses.get(0);
        assertEquals(10L, response.getId());
        assertEquals(SIGNED_URL, response.getImageUrl());
        assertEquals("Sunset", response.getCaption());
        assertEquals(user.getId(), response.getUploadedById());
        assertEquals(user.getName(), response.getUploadedByName());
        assertEquals(user.getProfilePicture(), response.getUploadedByProfilePicture());
        assertEquals(createdAt, response.getCreatedAt());
    }

    @Test
    void shouldNotLoadPhotosWhenAuthorizationFails() {
        IllegalArgumentException authorizationError =
                new IllegalArgumentException("You are not a member of this trip");
        when(tripAuthorizationService.getCurrentMember(TRIP_ID, USER_EMAIL))
                .thenThrow(authorizationError);

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> tripPhotoService.getTripPhotos(TRIP_ID, USER_EMAIL)
        );

        assertSame(authorizationError, thrown);
        verify(tripPhotoRepository, never())
                .findByTripIdOrderByCreatedAtDesc(any());
    }

    @Test
    void shouldUploadPhotoAndNormalizeCaption() {
        stubAuthorizedUpload();
        when(tripPhotoRepository.save(any(TripPhoto.class)))
                .thenAnswer(invocation -> savedCopy(invocation.getArgument(0)));
        when(tripPhotoStorageService.createSignedUrl(STORAGE_PATH))
                .thenReturn(SIGNED_URL);

        TripPhotoResponse response = tripPhotoService.uploadPhoto(
                TRIP_ID,
                file,
                "  First day in Rome  ",
                USER_EMAIL
        );

        ArgumentCaptor<TripPhoto> captor = ArgumentCaptor.forClass(TripPhoto.class);
        verify(tripPhotoRepository).save(captor.capture());
        TripPhoto photo = captor.getValue();
        assertSame(trip, photo.getTrip());
        assertSame(user, photo.getUploadedBy());
        assertEquals(STORAGE_PATH, photo.getStoragePath());
        assertEquals("First day in Rome", photo.getCaption());
        assertEquals(SIGNED_URL, response.getImageUrl());
        verify(tripPhotoStorageService, never()).delete(any());
    }

    @Test
    void shouldStoreBlankCaptionAsNull() {
        stubAuthorizedUpload();
        when(tripPhotoRepository.save(any(TripPhoto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(tripPhotoStorageService.createSignedUrl(STORAGE_PATH))
                .thenReturn(SIGNED_URL);

        tripPhotoService.uploadPhoto(TRIP_ID, file, "   ", USER_EMAIL);

        ArgumentCaptor<TripPhoto> captor = ArgumentCaptor.forClass(TripPhoto.class);
        verify(tripPhotoRepository).save(captor.capture());
        assertNull(captor.getValue().getCaption());
    }

    @Test
    void shouldRejectCaptionLongerThanLimitBeforeUploading() {
        when(tripAuthorizationService.getCurrentMember(TRIP_ID, USER_EMAIL))
                .thenReturn(membership);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tripPhotoService.uploadPhoto(
                        TRIP_ID, file, "a".repeat(251), USER_EMAIL
                )
        );

        assertEquals("Caption must not exceed 250 characters", exception.getMessage());
        verify(tripPhotoStorageService, never()).upload(any(), any());
        verify(tripPhotoRepository, never()).save(any());
    }

    @Test
    void shouldDeleteStoredFileWhenDatabaseSaveFails() {
        RuntimeException databaseError = new RuntimeException("Database unavailable");
        stubAuthorizedUpload();
        when(tripPhotoRepository.save(any(TripPhoto.class)))
                .thenThrow(databaseError);

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> tripPhotoService.uploadPhoto(TRIP_ID, file, "Caption", USER_EMAIL)
        );

        assertSame(databaseError, thrown);
        verify(tripPhotoStorageService).delete(STORAGE_PATH);
        verify(tripPhotoStorageService, never()).createSignedUrl(any());
    }

    @Test
    void shouldPreserveCleanupFailureAsSuppressedException() {
        RuntimeException databaseError = new RuntimeException("Database unavailable");
        RuntimeException cleanupError = new RuntimeException("Storage unavailable");
        stubAuthorizedUpload();
        when(tripPhotoRepository.save(any(TripPhoto.class)))
                .thenThrow(databaseError);
        doThrow(cleanupError)
                .when(tripPhotoStorageService).delete(STORAGE_PATH);

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> tripPhotoService.uploadPhoto(TRIP_ID, file, "Caption", USER_EMAIL)
        );

        assertSame(databaseError, thrown);
        assertEquals(1, thrown.getSuppressed().length);
        assertSame(cleanupError, thrown.getSuppressed()[0]);
    }

    private void stubAuthorizedUpload() {
        when(tripAuthorizationService.getCurrentMember(TRIP_ID, USER_EMAIL))
                .thenReturn(membership);
        when(tripPhotoStorageService.upload(TRIP_ID, file))
                .thenReturn(new StoredPhoto(STORAGE_PATH));
    }

    private TripPhoto savedPhoto(String caption, LocalDateTime createdAt) {
        return TripPhoto.builder()
                .id(10L)
                .trip(trip)
                .uploadedBy(user)
                .storagePath(STORAGE_PATH)
                .caption(caption)
                .createdAt(createdAt)
                .build();
    }

    private TripPhoto savedCopy(TripPhoto photo) {
        return TripPhoto.builder()
                .id(10L)
                .trip(photo.getTrip())
                .uploadedBy(photo.getUploadedBy())
                .storagePath(photo.getStoragePath())
                .caption(photo.getCaption())
                .createdAt(LocalDateTime.of(2026, 8, 20, 11, 0))
                .build();
    }
}
