export interface ItineraryItem{
    id: number;
    tripId: number;
    createdById: number;
    createdByName: string;
    createdByProfilePicture: string | null;
    activityDate: string;
    startTime: string;
    endTime: string | null;
    title: string;
    description: string | null;
    place: string | null;
    createdAt: string;
    updatedAt: string;
}

export interface ItineraryItemRequest {
    activityDate: string;
    startTime: string;
    endTime?: string | null;
    title: string;
    description?: string | null;
    place?: string | null;
}