export interface Trip {
    id: number;
    name: string;
    description: string;
    destination: string;
    startDate: string;
    endDate: string;
    coverImage: string | null;
    createdAt: string;
    updatedAt: string;
}

export interface TripRequest {
    name: string;
    description?: string;
    destination: string;
    startDate: string;
    endDate: string;
}

export interface TripMember {
    userId: number;
    name: string;
    email: string;
    profilePicture?: string;
    role: "OWNER" | "MEMBER";
}

export interface AddTripMemberRequest {
    email: string;
}

export interface UserSearchResult {
    id: number;
    name: string;
    email: string;
    profilePicture: string | null;
}
