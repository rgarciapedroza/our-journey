package com.ourjourney.backend.service;

import org.springframework.web.multipart.MultipartFile;

public interface SupabaseStorageService {

    String uploadProfilePicture(
            Long userId,
            MultipartFile file
    );

    void deleteProfilePicture(
            Long userId
    );
}