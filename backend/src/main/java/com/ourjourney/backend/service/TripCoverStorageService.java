package com.ourjourney.backend.service;

import org.springframework.web.multipart.MultipartFile;

import com.ourjourney.backend.dto.StoredPhoto;

public interface TripCoverStorageService {

    StoredPhoto upload(Long tripId, MultipartFile file);

    String createSignedUrl(String storagePath);

    void delete(String storagePath);
}
