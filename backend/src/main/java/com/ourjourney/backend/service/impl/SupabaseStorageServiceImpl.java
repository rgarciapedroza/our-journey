package com.ourjourney.backend.service.impl;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import com.ourjourney.backend.config.SupabaseConfig;
import com.ourjourney.backend.service.SupabaseStorageService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SupabaseStorageServiceImpl
        implements SupabaseStorageService {

    private final SupabaseConfig supabaseConfig;

    private final RestClient restClient;

    @Override
    public String uploadProfilePicture(
            Long userId,
            MultipartFile file
    ) {

        if (file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Profile picture cannot be empty"
            );
        }

        String contentType = file.getContentType();

        if (contentType == null ||
                !contentType.startsWith("image/")) {

            throw new IllegalArgumentException(
                    "File must be an image"
            );
        }

        String extension =
                getExtension(file.getOriginalFilename());

        String filePath =
                "users/" + userId + "/profile" + extension;

        try {

            restClient.put()
                    .uri(
                        supabaseConfig.getUrl()
                            + "/storage/v1/object/"
                            + supabaseConfig.getBucket()
                            + "/" + filePath
                    )
                    .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer "
                            + supabaseConfig.getKey()
                    )
                    .header(
                        "apikey",
                        supabaseConfig.getKey()
                    )
                    .header(
                        HttpHeaders.CONTENT_TYPE,
                        contentType
                    )
                    .header(
                        "x-upsert",
                        "true"
                    )
                    .body(file.getBytes())
                    .retrieve()
                    .toBodilessEntity();

        } catch (IOException e) {

            throw new IllegalArgumentException(
                    "Could not read profile picture",
                    e
            );
        }

        return supabaseConfig.getUrl()
                + "/storage/v1/object/public/"
                + supabaseConfig.getBucket()
                + "/" + filePath;
    }

    @Override
    public void deleteProfilePicture(Long userId) {

        String[] extensions = {
                ".jpg",
                ".jpeg",
                ".png",
                ".webp"
        };

        for (String extension : extensions) {

            String filePath =
                    "users/" + userId
                    + "/profile" + extension;

            try {

                restClient.delete()
                        .uri(
                            supabaseConfig.getUrl()
                                + "/storage/v1/object/"
                                + supabaseConfig.getBucket()
                                + "/" + filePath
                        )
                        .header(
                            HttpHeaders.AUTHORIZATION,
                            "Bearer "
                                + supabaseConfig.getKey()
                        )
                        .header(
                            "apikey",
                            supabaseConfig.getKey()
                        )
                        .retrieve()
                        .toBodilessEntity();

            } catch (Exception ignored) {
            }
        }
    }

    private String getExtension(String filename) {

        if (filename == null || !filename.contains(".")) {
            return ".jpg";
        }

        String extension = filename
                .substring(filename.lastIndexOf("."))
                .toLowerCase();

        if (!extension.equals(".jpg")
                && !extension.equals(".jpeg")
                && !extension.equals(".png")
                && !extension.equals(".webp")) {

            throw new IllegalArgumentException(
                    "Unsupported image format"
            );
        }

        return extension;
    }
}