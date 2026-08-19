package com.ourjourney.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ourjourney.backend.entity.TripPhoto;

public interface TripPhotoRepository extends JpaRepository<TripPhoto, Long> {
    List<TripPhoto> findByTripIdOrderByCreatedAtDesc(Long tripId);
}
