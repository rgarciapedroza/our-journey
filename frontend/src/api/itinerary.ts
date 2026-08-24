import { apiFetch } from "./client";

import type {
    ItineraryItem,
    ItineraryItemRequest,
} from "../types/itineraryItem";

export async function getItineraryItems(tripId:number): Promise<ItineraryItem[]>{
    return apiFetch<ItineraryItem[]>(`/api/trips/${tripId}/itinerary`);
}

export async function createItineraryItem(
    tripId: number,
    request: ItineraryItemRequest
): Promise<ItineraryItem>{
    return apiFetch<ItineraryItem>(`/api/trips/${tripId}/itinerary`,{
        method: "POST",
        body: JSON.stringify(request),
    });
}

export async function updateItineraryItem(
    tripId: number,
    itemId: number,
    request: ItineraryItemRequest
): Promise<ItineraryItem>{
    return apiFetch<ItineraryItem>(`/api/trips/${tripId}/itinerary/${itemId}`,{
        method: "PUT",
        body: JSON.stringify(request),
    });
}

export async function deleteItineraryItem(
    tripId: number,
    itemId: number
): Promise <void>{await apiFetch<void>(`/api/trips/${tripId}/itinerary/${itemId}`,
    {
        method: "DELETE",
    }
);}