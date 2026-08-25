package com.ourjourney.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.ourjourney.backend.config.SupabaseConfig;
import com.ourjourney.backend.dto.StoredPhoto;
import com.ourjourney.backend.service.impl.TripPhotoStorageServiceImpl;

@ExtendWith(MockitoExtension.class)
class TripPhotoStorageServiceImplTest {

    private static final String SUPABASE_URL = "https://project.supabase.co";
    private static final String BUCKET = "trip-photos";
    private static final String API_KEY = "test-service-key";

    @Mock
    private SupabaseConfig supabaseConfig;

    private MockRestServiceServer server;
    private TripPhotoStorageServiceImpl storageService;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        storageService = new TripPhotoStorageServiceImpl(
                supabaseConfig,
                builder.build()
        );

    }

    @Test
    void shouldUploadSupportedImageUsingUniqueStoragePath() {
        stubSupabaseConfig();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "holiday.jpg",
                "image/jpeg",
                "image-content".getBytes()
        );

        server.expect(requestTo(Matchers.matchesPattern(
                        SUPABASE_URL
                                + "/storage/v1/object/"
                                + BUCKET
                                + "/trips/7/[a-f0-9\\-]{36}\\.jpg"
                )))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer " + API_KEY))
                .andExpect(header("apikey", API_KEY))
                .andExpect(header("Content-Type", "image/jpeg"))
                .andRespond(withSuccess());

        StoredPhoto storedPhoto = storageService.upload(7L, file);

        assertTrue(
                storedPhoto.getStoragePath()
                        .matches("trips/7/[a-f0-9\\-]{36}\\.jpg")
        );
        server.verify();
    }

    @Test
    void shouldRejectEmptyFileWithoutCallingSupabase() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.png", "image/png", new byte[0]
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> storageService.upload(1L, file)
        );

        assertEquals("Photo cannot be empty", exception.getMessage());
        server.verify();
    }

    @Test
    void shouldRejectUnsupportedContentTypeWithoutCallingSupabase() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "document.pdf", "application/pdf", "content".getBytes()
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> storageService.upload(1L, file)
        );

        assertEquals("Unsupported image format", exception.getMessage());
        server.verify();
    }

    @Test
    void shouldRejectFileLargerThanFiveMegabytesWithoutCallingSupabase() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large.jpg",
                "image/jpeg",
                new byte[(5 * 1024 * 1024) + 1]
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> storageService.upload(1L, file)
        );

        assertEquals("Photo must not exceed 5 MB", exception.getMessage());
        server.verify();
    }

    @Test
    void shouldCreateSignedUrl() {
        stubSupabaseConfig();

        String storagePath = "trips/1/photo.jpg";

        server.expect(requestTo(
                        SUPABASE_URL
                                + "/storage/v1/object/sign/"
                                + BUCKET
                                + "/"
                                + storagePath
                ))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer " + API_KEY))
                .andRespond(withSuccess(
                        "{\"signedURL\":\"/object/sign/trip-photos/trips/1/photo.jpg?token=abc\"}",
                        MediaType.APPLICATION_JSON
                ));

        String signedUrl = storageService.createSignedUrl(storagePath);

        assertEquals(
                SUPABASE_URL
                        + "/storage/v1/object/sign/trip-photos/trips/1/photo.jpg?token=abc",
                signedUrl
        );
        server.verify();
    }

    @Test
    void shouldRejectSignedUrlResponseWithoutUrl() {
        stubSupabaseConfig();
        String storagePath = "trips/1/photo.jpg";

        server.expect(requestTo(
                        SUPABASE_URL
                                + "/storage/v1/object/sign/"
                                + BUCKET
                                + "/"
                                + storagePath
                ))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> storageService.createSignedUrl(storagePath)
        );

        assertEquals("A signed URL was not received", exception.getMessage());
        server.verify();
    }

    @Test
    void shouldDeleteStoredPhoto() {
        stubSupabaseConfig();
        String storagePath = "trips/1/photo.jpg";

        server.expect(requestTo(
                        SUPABASE_URL
                                + "/storage/v1/object/"
                                + BUCKET
                                + "/"
                                + storagePath
                ))
                .andExpect(method(HttpMethod.DELETE))
                .andExpect(header("Authorization", "Bearer " + API_KEY))
                .andExpect(header("apikey", API_KEY))
                .andRespond(withSuccess());

        storageService.delete(storagePath);

        server.verify();
    }

    @Test
    void shouldWrapSupabaseUploadFailure() {
        stubSupabaseConfig();

        MockMultipartFile file = new MockMultipartFile(
                "file", "holiday.webp", "image/webp", "image-content".getBytes()
        );

        server.expect(requestTo(Matchers.matchesPattern(
                        SUPABASE_URL
                                + "/storage/v1/object/"
                                + BUCKET
                                + "/trips/3/[a-f0-9\\-]{36}\\.webp"
                )))
                .andRespond(withServerError());

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> storageService.upload(3L, file)
        );

        assertEquals("Could not upload trip photo", exception.getMessage());
        server.verify();
    }

    private void stubSupabaseConfig() {
        when(supabaseConfig.getUrl()).thenReturn(SUPABASE_URL);
        when(supabaseConfig.getTripPhotosBucket()).thenReturn(BUCKET);
        when(supabaseConfig.getKey()).thenReturn(API_KEY);
    }
}
