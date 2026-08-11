import type { Trip, TripRequest } from "../types/trip";
import { apiFetch } from "./client";

export async function getTrips(): Promise<Trip[]> {
    return apiFetch<Trip[]>("/api/trips");
}

export async function getTrip(id: number): Promise<Trip> {
    return apiFetch<Trip>(`/api/trips/${id}`);
}

export async function createTrip(
    request: TripRequest
): Promise<Trip> {

    return apiFetch<Trip>("/api/trips", {
        method: "POST",
        body: JSON.stringify(request),
    });
}

export async function updateTrip(
    id: number,
    request: TripRequest
): Promise<Trip> {

    return apiFetch<Trip>(`/api/trips/${id}`, {
        method: "PUT",
        body: JSON.stringify(request),
    });
}

export async function deleteTrip(id: number): Promise<void> {
    return apiFetch<void>(`/api/trips/${id}`, {
        method: "DELETE",
    });
}