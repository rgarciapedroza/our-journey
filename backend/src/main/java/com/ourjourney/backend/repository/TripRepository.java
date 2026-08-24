package com.ourjourney.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ourjourney.backend.entity.Trip;

public interface TripRepository extends JpaRepository<Trip, Long> {
    
}
