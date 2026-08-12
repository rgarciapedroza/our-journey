package com.ourjourney.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ourjourney.backend.entity.TripMember;

public interface TripMemberRepository
        extends JpaRepository<TripMember, Long> {

    List<TripMember> findByTripId(Long tripId);

    Optional<TripMember> findByTripIdAndUserId(
            Long tripId,
            Long userId
    );

    boolean existsByTripIdAndUserId(
            Long tripId,
            Long userId
    );

    void deleteByTripIdAndUserId(
            Long tripId,
            Long userId
    );
}