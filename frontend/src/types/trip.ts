export interface Trip {
    id: number;
    name: string;
    description: string;
    destination: string;
    startDate: string;
    endDate: string;
    coverImage: string;
    createdAt: string;
    updatedAt: string;
}

export interface TripRequest {
    name: string;
    description?: string;
    destination: string;
    startDate: string;
    endDate: string;
    coverImage?: string;
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