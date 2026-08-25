# Our Journey Frontend

React and TypeScript client for [Our Journey](../README.md), a collaborative travel planning application.

The frontend provides authenticated navigation, trip and participant management, shared itineraries, photo galleries, account settings, and media-upload workflows backed by the Spring Boot REST API.

## Technology Stack

- React 19
- TypeScript
- Vite
- React Router
- CSS Modules

## Project Structure

```text
src/
|-- api/          Typed functions for communicating with the REST API
|-- assets/       Static images and frontend assets
|-- components/   Reusable interface and navigation components
|-- context/      Shared authentication state
|-- pages/        Route-level application pages
|-- styles/       CSS Modules and global styles
|-- types/        Shared TypeScript models and request types
`-- utils/        Date and presentation utilities
```

## Local Development

### Prerequisites

- Node.js and npm
- The Our Journey backend running on `http://localhost:8080`, or another configured API URL

### Install dependencies

From the `frontend` directory:

```bash
npm ci
```

### Configure the API URL

Create `frontend/.env.local` when running the frontend directly with Vite:

```env
VITE_API_URL=http://localhost:8080
```

Only variables prefixed with `VITE_` are exposed to browser code. Never place server credentials or Supabase server keys in a frontend environment file.

### Start the development server

```bash
npm run dev
```

Vite serves the application on `http://localhost:5173` by default.

## Available Scripts

| Command | Purpose |
|---|---|
| `npm run dev` | Start the Vite development server with hot module replacement. |
| `npm run build` | Type-check the application and create a production build. |
| `npm run lint` | Run ESLint across the frontend source. |
| `npm run preview` | Preview the production build locally. |

## Docker

The recommended full-stack setup uses Docker Compose from the repository root. The root `.env` value for `VITE_API_URL` is provided to the frontend image during its build.

```bash
docker compose up --build
```

With Docker Compose, the frontend is available at `http://localhost:3000`.

## API Integration

Requests are centralized in `src/api`. The shared API client attaches the JWT stored after login, preserves multipart request boundaries for file uploads, and converts unsuccessful HTTP responses into typed `ApiError` instances.

## Further Documentation

- [Project overview and setup](../README.md)
- [REST API reference](../docs/api.md)
- [Database model](../docs/database.md)
- [Environment variable template](../.env.example)
