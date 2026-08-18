package com.ourjourney.backend.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestClient;

import com.ourjourney.backend.config.SupabaseConfig;
import com.ourjourney.backend.service.impl.SupabaseStorageServiceImpl;

@ExtendWith(MockitoExtension.class)
class SupabaseStorageServiceImplTest {

    @Mock
    private SupabaseConfig supabaseConfig;

    @Mock
    private RestClient restClient;

    private SupabaseStorageServiceImpl storageService;

    @BeforeEach
    void setUp() {
        storageService = new SupabaseStorageServiceImpl(
                supabaseConfig,
                restClient
        );
    }

    @Test
    void shouldRejectEmptyFile() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "profile.png",
                        "image/png",
                        new byte[0]
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> storageService.uploadProfilePicture(1L, file)
        );
    }

    @Test
    void shouldRejectNonImageFile() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "document.pdf",
                        "application/pdf",
                        "test".getBytes()
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> storageService.uploadProfilePicture(1L, file)
        );
    }

    @Test
    void shouldRejectUnsupportedImageFormat() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "profile.gif",
                        "image/gif",
                        "test".getBytes()
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> storageService.uploadProfilePicture(1L, file)
        );
    }
}