# Database Model

Our Journey uses PostgreSQL through Spring Data JPA. The model separates trip membership from users and trips so that multiple users can collaborate on the same trip with different roles.

## Entity relationship diagram

```mermaid
erDiagram
    USERS ||--o{ TRIP_MEMBERS : joins
    TRIPS ||--o{ TRIP_MEMBERS : includes
    USERS ||--o{ TRIP_PHOTOS : uploads
    TRIPS ||--o{ TRIP_PHOTOS : contains
    USERS ||--o{ ITINERARY_ITEMS : creates
    TRIPS ||--o{ ITINERARY_ITEMS : schedules

    USERS {
        bigint id PK
        varchar name
        varchar email
        varchar password
        varchar profile_picture
        timestamp created_at
        timestamp updated_at
    }

    TRIPS {
        bigint id PK
        varchar name
        varchar description
        varchar destination
        date start_date
        date end_date
        varchar cover_image
        timestamp created_at
        timestamp updated_at
    }

    TRIP_MEMBERS {
        bigint id PK
        bigint trip_id FK
        bigint user_id FK
        varchar role
        timestamp joined_at
    }

    TRIP_PHOTOS {
        bigint id PK
        bigint trip_id FK
        bigint uploaded_by FK
        varchar storage_path
        varchar caption
        timestamp created_at
    }

    ITINERARY_ITEMS {
        bigint id PK
        bigint trip_id FK
        bigint created_by FK
        date activity_date
        time start_time
        time end_time
        varchar title
        varchar description
        varchar place
        timestamp created_at
        timestamp updated_at
    }
```

## Entity responsibilities

### User

Stores account information, the BCrypt password hash and the optional profile-picture URL.

### Trip

Stores the shared trip information and the private storage path of its optional cover image. Trip ownership is not stored directly on this entity; it is represented by an associated `TripMember` with the `OWNER` role.

### TripMember

Links a user to a trip. Its role is either `OWNER` or `MEMBER`. The database enforces a unique constraint on the combination of `trip_id` and `user_id`, preventing the same user from joining one trip more than once.

### TripPhoto

Stores photo metadata, including the private Supabase storage path, optional caption, uploader and creation time. The database stores the path rather than a signed URL because signed URLs are temporary and generated when responses are requested.

### ItineraryItem

Represents one activity in the shared itinerary. Each item belongs to a trip, records its creator and contains its date, time range, title, optional description and optional place.

## Relationship summary

| Parent | Child | Relationship |
|---|---|---|
| `User` | `TripMember` | A user can belong to multiple trips. |
| `Trip` | `TripMember` | A trip can contain multiple participants. |
| `User` | `TripPhoto` | A user can upload multiple photos. |
| `Trip` | `TripPhoto` | A trip can contain multiple photos. |
| `User` | `ItineraryItem` | A user can create multiple itinerary activities. |
| `Trip` | `ItineraryItem` | A trip can contain multiple itinerary activities. |

All primary keys use database-generated `BIGINT` identity values. Foreign-key associations are loaded lazily by JPA.
