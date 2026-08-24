package com.ourjourney.backend.repository;

import com.ourjourney.backend.entity.User;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("""
            SELECT user
            FROM User user
            WHERE user.id NOT IN (
                SELECT member.user.id
                FROM TripMember member
                WHERE member.trip.id = :tripId
            )
            AND (
                LOWER(user.name) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(user.email) LIKE LOWER(CONCAT('%', :query, '%'))
            )
            ORDER BY user.name ASC
            """)
    List<User> searchAvailableTripMembers(
            @Param("tripId") Long tripId,
            @Param("query") String query,
            Pageable pageable
    );
}
