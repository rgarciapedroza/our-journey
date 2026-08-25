# REST API

This document describes the HTTP API exposed by the Our Journey Spring Boot backend.

## Base URL

```text
http://localhost:8080/api
```

## Authentication

Registration and login are public. Every other endpoint requires the JWT returned by the login endpoint:

```http
Authorization: Bearer <token>
```

The API is stateless. Missing, invalid or expired tokens produce `401 Unauthorized`.

## Common responses

| Status | Meaning |
|---|---|
| `200 OK` | The operation completed successfully. |
| `201 Created` | A new resource was created. |
| `204 No Content` | The operation completed and has no response body. |
| `400 Bad Request` | The request or uploaded file is invalid. |
| `401 Unauthorized` | Authentication failed or the password is incorrect. |
| `403 Forbidden` | The authenticated user does not have permission. |
| `404 Not Found` | The requested resource does not exist. |

Handled application errors use this structure:

```json
{
  "timestamp": "2026-08-25T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Trip not found",
  "path": "/api/trips/99"
}
```

Validation errors may use Spring Boot's default validation response.

## Authentication endpoints

| Method | Endpoint | Authentication | Request | Success |
|---|---|---|---|---|
| `POST` | `/api/auth/register` | Public | `RegisterRequest` | `201` with `UserResponse` |
| `POST` | `/api/auth/login` | Public | `LoginRequest` | `200` with `LoginResponse` |
| `GET` | `/api/auth/me` | Required | None | `200` with `UserResponse` |

### RegisterRequest

```json
{
  "name": "Jane Doe",
  "email": "jane@example.com",
  "password": "securePassword",
  "confirmPassword": "securePassword"
}
```

- `name`: required, 3-50 characters
- `email`: required, valid email address
- `password`: required, 8-100 characters
- `confirmPassword`: required and must match `password`

### LoginRequest

```json
{
  "email": "jane@example.com",
  "password": "securePassword"
}
```

`LoginResponse` contains `id`, `name`, `email`, `profilePicture` and `token`.

## User profile endpoints

| Method | Endpoint | Request | Success |
|---|---|---|---|
| `PUT` | `/api/users/me` | `UserProfileUpdateRequest` | `200` with `UserProfileResponse` |
| `PUT` | `/api/users/me/profile-picture` | Multipart field `file` | `200` with `UserProfileResponse` |
| `POST` | `/api/users/verify-password` | `VerifyPasswordRequest` | `204` |
| `PUT` | `/api/users/me/password` | `ChangePasswordRequest` | `204` |

All profile endpoints require authentication.

### Update profile

```json
{
  "name": "Jane Smith"
}
```

The name is required and must not exceed 100 characters. The email address cannot be changed through this endpoint.

### Verify password

```json
{
  "currentPassword": "securePassword"
}
```

Returns `204 No Content` when the password is correct and `401 Unauthorized` otherwise.

### Change password

```json
{
  "currentPassword": "securePassword",
  "newPassword": "newSecurePassword",
  "confirmNewPassword": "newSecurePassword"
}
```

The new password must contain at least six characters and its confirmation must match.

## Trip endpoints

| Method | Endpoint | Request | Success | Permission |
|---|---|---|---|---|
| `GET` | `/api/trips` | None | `200` with `TripResponse[]` | Authenticated user |
| `GET` | `/api/trips/{id}` | None | `200` with `TripResponse` | Trip member |
| `POST` | `/api/trips` | `TripRequest` | `201` with `TripResponse` | Authenticated user |
| `PUT` | `/api/trips/{id}` | `TripRequest` | `200` with `TripResponse` | Trip owner |
| `DELETE` | `/api/trips/{id}` | None | `204` | Trip owner |

### TripRequest

The same request is used to create and update a trip:

```json
{
  "name": "Weekend in Barcelona",
  "description": "Architecture, food and the beach",
  "destination": "Barcelona",
  "startDate": "2026-10-30",
  "endDate": "2026-11-02"
}
```

- `name`: required, 3-100 characters
- `description`: optional, maximum 1000 characters
- `destination`: required
- `startDate` and `endDate`: required ISO dates (`YYYY-MM-DD`)

`TripResponse` contains `id`, the request fields, `coverImage`, `createdAt` and `updatedAt`.

## Trip cover endpoints

| Method | Endpoint | Request | Success | Permission |
|---|---|---|---|---|
| `PUT` | `/api/trips/{id}/cover` | Multipart field `file` | `200` with `TripResponse` | Trip owner |
| `DELETE` | `/api/trips/{id}/cover` | None | `204` | Trip owner |

Cover uploads use `multipart/form-data`. Accepted formats are JPEG, PNG and WebP, with a maximum file size of 5 MB. Covers are stored privately and returned through temporary signed URLs.

## Trip member endpoints

| Method | Endpoint | Request | Success | Permission |
|---|---|---|---|---|
| `GET` | `/api/trips/{tripId}/members` | None | `200` with `TripMemberResponse[]` | Authenticated user |
| `GET` | `/api/trips/{tripId}/members/search?query={value}` | Query parameter `query` | `200` with `UserSearchResponse[]` | Trip owner |
| `POST` | `/api/trips/{tripId}/members` | `AddTripMemberRequest` | `201` with `TripMemberResponse` | Trip owner |
| `DELETE` | `/api/trips/{tripId}/members/{userId}` | None | `204` | Trip owner |

### AddTripMemberRequest

```json
{
  "email": "traveller@example.com"
}
```

Member responses include the user's ID, name, email, profile picture and trip role. Search results include the ID, name, email and profile picture of users who can be added.

The search query is trimmed, must contain at least two characters and returns at most ten available users.

## Trip photo endpoints

| Method | Endpoint | Request | Success | Permission |
|---|---|---|---|---|
| `GET` | `/api/trips/{tripId}/photos` | None | `200` with `TripPhotoResponse[]` | Trip member |
| `POST` | `/api/trips/{tripId}/photos` | Multipart fields `file` and optional `caption` | `201` with `TripPhotoResponse` | Trip member |
| `DELETE` | `/api/trips/{tripId}/photos/{photoId}` | None | `204` | Photo uploader |

Photo uploads use `multipart/form-data`. Accepted formats are JPEG, PNG and WebP, with a maximum file size of 5 MB. Photos are stored privately and returned through temporary signed URLs.

`TripPhotoResponse` contains:

- `id`
- `imageUrl`
- `caption`
- `uploadedById`
- `uploadedByName`
- `uploadedByProfilePicture`
- `createdAt`

## Itinerary endpoints

| Method | Endpoint | Request | Success | Permission |
|---|---|---|---|---|
| `GET` | `/api/trips/{tripId}/itinerary` | None | `200` with `ItineraryItemResponse[]` | Trip member |
| `POST` | `/api/trips/{tripId}/itinerary` | `ItineraryItemRequest` | `201` with `ItineraryItemResponse` | Trip member |
| `PUT` | `/api/trips/{tripId}/itinerary/{itemId}` | `ItineraryItemRequest` | `200` with `ItineraryItemResponse` | Trip member |
| `DELETE` | `/api/trips/{tripId}/itinerary/{itemId}` | None | `204` | Trip member |

### ItineraryItemRequest

```json
{
  "activityDate": "2026-10-31",
  "startTime": "10:00:00",
  "endTime": "12:30:00",
  "title": "Visit the Sagrada Familia",
  "description": "Meet at the main entrance",
  "place": "Sagrada Familia"
}
```

- `activityDate`: required ISO date (`YYYY-MM-DD`)
- `startTime`: required ISO time
- `endTime`: optional ISO time
- `title`: required, maximum 120 characters
- `description`: optional, maximum 1000 characters
- `place`: optional, maximum 250 characters

`ItineraryItemResponse` adds the item ID, trip ID, creator information, and creation and update timestamps.
