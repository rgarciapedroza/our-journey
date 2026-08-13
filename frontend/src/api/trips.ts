import type { Trip, TripRequest, TripMember, AddTripMemberRequest } from "../types/trip";
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

export async function getTripMembers(
    tripId: number
): Promise<TripMember[]> {
    return apiFetch<TripMember[]>(
        `/api/trips/${tripId}/members`
    );
}


export async function addTripMember(
    tripId: number,
    request: AddTripMemberRequest
): Promise<TripMember> {
    return apiFetch<TripMember>(
        `/api/trips/${tripId}/members`,
        {
            method: "POST",
            body: JSON.stringify(request),
        }
    );
}


export async function removeTripMember(
    tripId: number,
    userId: number
): Promise<void> {
    return apiFetch<void>(
        `/api/trips/${tripId}/members/${userId}`,
        {
            method: "DELETE",
        }
    );
}