package com.ourjourney.backend.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ourjourney.backend.dto.ItineraryItemRequest;
import com.ourjourney.backend.dto.ItineraryItemResponse;
import com.ourjourney.backend.entity.ItineraryItem;
import com.ourjourney.backend.entity.Trip;
import com.ourjourney.backend.entity.TripMember;
import com.ourjourney.backend.exception.BadRequestException;
import com.ourjourney.backend.exception.ResourceNotFoundException;
import com.ourjourney.backend.repository.ItineraryItemRepository;
import com.ourjourney.backend.service.ItineraryItemService;
import com.ourjourney.backend.service.TripAuthorizationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItineraryItemServiceImpl implements ItineraryItemService {

    private final ItineraryItemRepository itineraryItemRepository;
    private final TripAuthorizationService tripAuthorizationService;


    @Override
    @Transactional(readOnly = true)
    public List<ItineraryItemResponse> getItems(Long tripId, String currentUserEmail) {
        tripAuthorizationService.getCurrentMember(
                tripId,
                currentUserEmail
        );

        return itineraryItemRepository
                .findByTripIdOrderByActivityDateAscStartTimeAsc(tripId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ItineraryItemResponse createItem(Long tripId, ItineraryItemRequest request, String currentUserEmail) {
       TripMember currentMember = tripAuthorizationService.getCurrentMember(tripId, currentUserEmail);
       
       validateRequest(request, currentMember.getTrip());

       ItineraryItem item = ItineraryItem.builder()
                .trip(currentMember.getTrip())
                .createdBy(currentMember.getUser())
                .activityDate(request.getActivityDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .title(request.getTitle().trim())
                .description(normalizeOptionalText(request.getDescription()))
                .place(normalizeOptionalText(request.getPlace()))
                .build();

        ItineraryItem savedItem = itineraryItemRepository.save(item);

        return toResponse(savedItem);
    }

    @Override
    @Transactional
    public ItineraryItemResponse updateItem(Long tripId, Long itemId, ItineraryItemRequest request, String currentUserEmail) {

        TripMember currentMember = tripAuthorizationService.getCurrentMember(tripId, currentUserEmail);

        ItineraryItem item = findItem(tripId, itemId);

        validateRequest(request, currentMember.getTrip());

        item.updateDetails(
            request.getActivityDate(),
            request.getStartTime(),
            request.getEndTime(),
            request.getTitle().trim(),
            normalizeOptionalText(request.getDescription()),
            normalizeOptionalText(request.getPlace())
            
        );

        return toResponse(item);
    }

    @Override
    @Transactional
    public void deleteItem(Long tripId, Long itemId, String currentUserEmail) {
        
        tripAuthorizationService.getCurrentMember(tripId, currentUserEmail);
        
        ItineraryItem item = findItem(tripId, itemId);
        
        itineraryItemRepository.delete(item);
    }

    private ItineraryItem findItem(Long tripId, Long itemId){
        
        return itineraryItemRepository
                .findByIdAndTripId(itemId, tripId)
                .orElseThrow(() -> new  ResourceNotFoundException("Itinerary item not found"));
    }

    private void validateRequest(ItineraryItemRequest request, Trip trip){

        if (
                request.getEndTime() != null
                && !request.getEndTime().isAfter(request.getStartTime())
        ) {
            throw new BadRequestException("End time must be after start time");
        }

        if (
                request.getActivityDate().isBefore(trip.getStartDate())
                || request.getActivityDate().isAfter(trip.getEndDate())
        ) {
            throw new BadRequestException("Activity date must be within the trip dates");
        }
    }
    
    private String normalizeOptionalText(String value){
        if (value == null){
            return null;
        }

        String normalizedValue = value.trim();

        return normalizedValue.isEmpty() ? null : normalizedValue;
    }

    private ItineraryItemResponse toResponse(ItineraryItem item){
        return ItineraryItemResponse.builder()
            .id(item.getId())
            .tripId(item.getTrip().getId())
            .createdById(item.getCreatedBy().getId())
            .createdByName(item.getCreatedBy().getName())
            .createdByProfilePicture(item.getCreatedBy().getProfilePicture())
            .activityDate(item.getActivityDate())
            .startTime(item.getStartTime())
            .endTime(item.getEndTime())
            .title(item.getTitle())
            .description(item.getDescription())
            .place(item.getPlace())
            .createdAt(item.getCreatedAt())
            .updatedAt(item.getUpdatedAt())
            .build();
    }
}
