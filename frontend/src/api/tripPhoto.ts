import type { TripPhoto } from "../types/tripPhoto";
import { apiFetch } from "./client";

export async function getTripPhotos(tripId: number): Promise<TripPhoto[]>{
    return apiFetch<TripPhoto[]>(`/api/trips/${tripId}/photos`);
}

export async function uploadTripPhoto(
    tripId:number,
    file: File,
    caption?: string,
    ): Promise<TripPhoto> {

        const formData = new FormData();
        formData.append("file", file);

        const normalizedCaption = caption?.trim();

        if (normalizedCaption) {
            formData.append("caption", normalizedCaption);
        }

        return apiFetch<TripPhoto>(
            `/api/trips/${tripId}/photos`,
            {
                method: "POST",
                body: formData,
            }
        );
    }

export async function deletePhoto(
    tripId: number,
    photoId: number,
): Promise<void>{
        await apiFetch<void>(
            `/api/trips/${tripId}/photos/${photoId}`,
            {
                method: "DELETE",
            }
        );
    }