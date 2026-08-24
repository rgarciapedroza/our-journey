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
import com.ourjourney.backend.service.impl.TripCoverStorageServiceImpl;

@ExtendWith(MockitoExtension.class)
class TripCoverStorageServiceImplTest {

    private static final String SUPABASE_URL = "https://project.supabase.co";
    private static final String BUCKET = "trip-covers";
    private static final String API_KEY = "test-service-key";

    @Mock
    private SupabaseConfig supabaseConfig;

    private MockRestServiceServer server;
    private TripCoverStorageServiceImpl storageService;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        storageService = new TripCoverStorageServiceImpl(
                supabaseConfig,
                builder.build()
        );
    }

    @Test
    void shouldUploadSupportedCover() {
        stubSupabaseConfig();
        MockMultipartFile file = new MockMultipartFile(
                "file", "cover.jpg", "image/jpeg", "image".getBytes()
        );

        server.expect(requestTo(Matchers.matchesPattern(
                        SUPABASE_URL
                                + "/storage/v1/object/" + BUCKET
                                + "/trips/7/cover/[a-f0-9\\-]{36}\\.jpg"
                )))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer " + API_KEY))
                .andExpect(header("apikey", API_KEY))
                .andExpect(header("Content-Type", "image/jpeg"))
                .andRespond(withSuccess());

        StoredPhoto storedPhoto = storageService.upload(7L, file);

        assertTrue(storedPhoto.getStoragePath().matches(
                "trips/7/cover/[a-f0-9\\-]{36}\\.jpg"
        ));
        server.verify();
    }

    @Test
    void shouldRejectEmptyCover() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "cover.png", "image/png", new byte[0]
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> storageService.upload(1L, file)
        );

        assertEquals("Trip cover cannot be empty", exception.getMessage());
        server.verify();
    }

    @Test
    void shouldRejectUnsupportedCoverFormat() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "cover.gif", "image/gif", new byte[]{1}
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> storageService.upload(1L, file)
        );

        assertEquals("Unsupported image format", exception.getMessage());
        server.verify();
    }

    @Test
    void shouldRejectCoverLargerThanFiveMegabytes() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cover.webp",
                "image/webp",
                new byte[(5 * 1024 * 1024) + 1]
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> storageService.upload(1L, file)
        );

        assertEquals("Trip cover must not exceed 5 MB", exception.getMessage());
        server.verify();
    }

    @Test
    void shouldCreateSignedCoverUrl() {
        stubSupabaseConfig();
        String path = "trips/1/cover/cover.jpg";

        server.expect(requestTo(
                        SUPABASE_URL + "/storage/v1/object/sign/"
                                + BUCKET + "/" + path
                ))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(
                        "{\"signedURL\":\"/object/sign/trip-covers/trips/1/cover/cover.jpg?token=abc\"}",
                        MediaType.APPLICATION_JSON
                ));

        String url = storageService.createSignedUrl(path);

        assertEquals(
                SUPABASE_URL
                        + "/storage/v1/object/sign/trip-covers/trips/1/cover/cover.jpg?token=abc",
                url
        );
        server.verify();
    }

    @Test
    void shouldDeleteStoredCover() {
        stubSupabaseConfig();
        String path = "trips/1/cover/cover.jpg";

        server.expect(requestTo(
                        SUPABASE_URL + "/storage/v1/object/" + BUCKET + "/" + path
                ))
                .andExpect(method(HttpMethod.DELETE))
                .andExpect(header("Authorization", "Bearer " + API_KEY))
                .andExpect(header("apikey", API_KEY))
                .andRespond(withSuccess());

        storageService.delete(path);

        server.verify();
    }

    @Test
    void shouldWrapSupabaseUploadFailure() {
        stubSupabaseConfig();
        MockMultipartFile file = new MockMultipartFile(
                "file", "cover.webp", "image/webp", new byte[]{1}
        );

        server.expect(requestTo(Matchers.matchesPattern(
                        SUPABASE_URL
                                + "/storage/v1/object/" + BUCKET
                                + "/trips/3/cover/[a-f0-9\\-]{36}\\.webp"
                )))
                .andRespond(withServerError());

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> storageService.upload(3L, file)
        );

        assertEquals("Could not upload trip cover", exception.getMessage());
        server.verify();
    }

    private void stubSupabaseConfig() {
        when(supabaseConfig.getUrl()).thenReturn(SUPABASE_URL);
        when(supabaseConfig.getTripCoversBucket()).thenReturn(BUCKET);
        when(supabaseConfig.getKey()).thenReturn(API_KEY);
    }
}
