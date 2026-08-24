export interface TripPhoto{
    id: number;
    imageUrl: string;
    caption: string | null;
    uploadedById: number;
    uploadedByName: string;
    uploadedByProfilePicture: string | null;
    createdAt: string;
}