package com.ourjourney.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ourjourney.backend.entity.ItineraryItem;

public interface ItineraryItemRepository extends JpaRepository<ItineraryItem, Long>{ 
    
    List<ItineraryItem> findByTripIdOrderByActivityDateAscStartTimeAsc(Long tripId);
    Optional<ItineraryItem> findByIdAndTripId(Long itemId, Long tripId);

}
