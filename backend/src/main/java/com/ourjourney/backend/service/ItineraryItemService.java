package com.ourjourney.backend.service;

import java.util.List;

import com.ourjourney.backend.dto.ItineraryItemRequest;
import com.ourjourney.backend.dto.ItineraryItemResponse;

public interface ItineraryItemService {
    List<ItineraryItemResponse> getItems(Long tripId, String currentUserEmail);

    ItineraryItemResponse createItem(Long tripId, ItineraryItemRequest request, String currentUserEmail);

    ItineraryItemResponse updateItem(Long tripId, Long itemId, ItineraryItemRequest request, String currentUserEmail);

    void deleteItem(Long tripId, Long itemId, String currentUserEmail);
}
