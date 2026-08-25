export interface LoginRequest {
    email: string;
    password: string;
}

export interface RegisterRequest {
    name: string;
    email: string;
    password: string;
    confirmPassword: string;
}

export interface User {
    id: number;
    name: string;
    email: string;
    profilePicture: string | null;
}

export interface LoginResponse extends User {
    token: string;
}