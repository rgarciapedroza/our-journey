import type { User } from "../types/auth";

import { apiFetch } from "./client";

export interface UserProfileUpdateRequest {
    name: string;
}

export interface ChangePasswordRequest {
    currentPassword: string;
    newPassword: string;
    confirmNewPassword: string;
}

export async function updateProfile(request: UserProfileUpdateRequest): Promise<User> {
    return apiFetch<User>("/api/users/me", {
        method: "PUT",
        body: JSON.stringify(request),
    });
}

export async function verifyCurrentPassword(currentPassword: string): Promise<void> {
    return apiFetch<void>("/api/users/verify-password", {
        method: "POST",
        body: JSON.stringify({ currentPassword }),
    });
}

export async function changePassword(
    request: ChangePasswordRequest
): Promise<void> {

    return apiFetch<void>(
        "/api/users/me/password",
        {
            method: "PUT",
            body: JSON.stringify(request),
        }
    );
}

export async function updateProfilePicture(
    file: File
): Promise<User> {

    const formData = new FormData();

    formData.append("file", file);

    return apiFetch<User>(
        "/api/users/me/profile-picture",
        {
            method: "PUT",
            body: formData,
        }
    );
}
