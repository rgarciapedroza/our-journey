package com.ourjourney.backend.service.impl;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import com.ourjourney.backend.config.SupabaseConfig;
import com.ourjourney.backend.dto.SignedUrlResponse;
import com.ourjourney.backend.dto.StoredPhoto;
import com.ourjourney.backend.service.TripCoverStorageService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TripCoverStorageServiceImpl implements TripCoverStorageService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final SupabaseConfig supabaseConfig;
    private final RestClient restClient;

    @Override
    public StoredPhoto upload(Long tripId, MultipartFile file) {
        validate(file);

        String contentType = file.getContentType();
        String storagePath = "trips/" + tripId + "/cover/"
                + UUID.randomUUID() + extensionFor(contentType);

        try {
            restClient.post()
                    .uri(objectUrl(storagePath))
                    .header(HttpHeaders.AUTHORIZATION, bearerToken())
                    .header("apikey", supabaseConfig.getKey())
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .body(file.getBytes())
                    .retrieve()
                    .toBodilessEntity();
        } catch (IOException exception) {
            throw new IllegalArgumentException("Could not read trip cover", exception);
        } catch (RestClientException exception) {
            throw new IllegalStateException("Could not upload trip cover", exception);
        }

        return new StoredPhoto(storagePath);
    }

    @Override
    public String createSignedUrl(String storagePath) {
        try {
            SignedUrlResponse response = restClient.post()
                    .uri(
                            supabaseConfig.getUrl()
                                    + "/storage/v1/object/sign/"
                                    + supabaseConfig.getTripCoversBucket()
                                    + "/" + storagePath
                    )
                    .header(HttpHeaders.AUTHORIZATION, bearerToken())
                    .header("apikey", supabaseConfig.getKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("expiresIn", 3600))
                    .retrieve()
                    .body(SignedUrlResponse.class);

            if (response == null || response.getSignedUrl() == null) {
                throw new IllegalStateException("A signed cover URL was not received");
            }

            return supabaseConfig.getUrl() + "/storage/v1" + response.getSignedUrl();
        } catch (RestClientException exception) {
            throw new IllegalStateException("Could not create trip cover URL", exception);
        }
    }

    @Override
    public void delete(String storagePath) {
        try {
            restClient.delete()
                    .uri(objectUrl(storagePath))
                    .header(HttpHeaders.AUTHORIZATION, bearerToken())
                    .header("apikey", supabaseConfig.getKey())
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            throw new IllegalStateException("Could not delete trip cover", exception);
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Trip cover cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Trip cover must not exceed 5 MB");
        }

        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Unsupported image format");
        }
    }

    private String extensionFor(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new IllegalArgumentException("Unsupported image format");
        };
    }

    private String objectUrl(String storagePath) {
        return supabaseConfig.getUrl()
                + "/storage/v1/object/"
                + supabaseConfig.getTripCoversBucket()
                + "/" + storagePath;
    }

    private String bearerToken() {
        return "Bearer " + supabaseConfig.getKey();
    }
}
