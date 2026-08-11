const API_URL = import.meta.env.VITE_API_URL;

export async function apiFetch<T>(
    endpoint: string,
    options: RequestInit = {}
): Promise<T> {

    const token = localStorage.getItem("token");

    const headers = new Headers(options.headers);

    headers.set("Content-Type", "application/json");

    if (token) {
        headers.set("Authorization", `Bearer ${token}`);
    }

    const response = await fetch(`${API_URL}${endpoint}`, {
        ...options,
        headers,
    });

    if (!response.ok) {
        throw new Error(`API error: ${response.status}`);
    }

    if (response.status === 204) {
        return undefined as T;
    }

    return response.json();
}