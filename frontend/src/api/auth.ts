import type {
    LoginRequest,
    LoginResponse,
    RegisterRequest,
    User,
} from "../types/auth";

import { apiFetch } from "./client";

export async function login(
    request: LoginRequest
): Promise<LoginResponse> {

    const response = await apiFetch<LoginResponse>(
        "/api/auth/login",
        {
            method: "POST",
            body: JSON.stringify(request),
        }
    );

    localStorage.setItem("token", response.token);

    return response;
}

export async function register(
    request: RegisterRequest
): Promise<User> {

    return apiFetch<User>(
        "/api/auth/register",
        {
            method: "POST",
            body: JSON.stringify(request),
        }
    );
}

export function logout(): void {
    localStorage.removeItem("token");
}

export function getToken(): string | null {
    return localStorage.getItem("token");
}

export async function getCurrentUser(): Promise<User> {
    return apiFetch<User>("/api/auth/me");
}