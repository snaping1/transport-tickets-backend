# Transport Tickets Server

Ktor + PostgreSQL backend for the transport tickets booking app.

## Requirements

- JDK 17+
- Gradle 8+
- PostgreSQL database (neon.tech recommended)
- Firebase project with service account key

## Setup

### 1. Firebase Service Account

1. Open [Firebase Console](https://console.firebase.google.com) → Project Settings → Service Accounts
2. Click **Generate new private key** → download `serviceAccountKey.json`
3. Place it in the project root (it's gitignored)

### 2. Neon.tech PostgreSQL

1. Register at [neon.tech](https://neon.tech)
2. Create a new project → copy the connection string
3. Format: `jdbc:postgresql://ep-xxx.region.aws.neon.tech/neondb?sslmode=require&user=xxx&password=xxx`

### 3. Environment Variables

Copy `.env.example` to `.env` and fill in the values:

```bash
cp .env.example .env
```

| Variable | Description |
|---|---|
| `DATABASE_URL` | JDBC URL for neon.tech PostgreSQL |
| `FIREBASE_CREDENTIALS_FILE` | Path to Firebase service account JSON |
| `FIREBASE_PROJECT_ID` | Firebase project ID |
| `PORT` | Server port (default: 8080) |

### 4. Build & Run

```bash
# Run locally
./gradlew run

# Build fat JAR
./gradlew shadowJar
java -jar build/libs/transport-tickets-server-0.0.1-all.jar

# Docker
docker build -t transport-server .
docker run -p 8080:8080 \
  -e DATABASE_URL="..." \
  -e FIREBASE_CREDENTIALS_FILE="/app/serviceAccountKey.json" \
  -e FIREBASE_PROJECT_ID="..." \
  -v /path/to/serviceAccountKey.json:/app/serviceAccountKey.json \
  transport-server
```

## API Endpoints

| Method | Path | Auth | Description |
|---|---|---|---|
| `POST` | `/auth/verify` | No | Verify Firebase token, create/update user |
| `GET` | `/routes` | Yes | List routes (filters: origin, destination, date) |
| `POST` | `/tickets/buy` | Yes | Buy a ticket |
| `GET` | `/tickets/my` | Yes | My tickets |
| `DELETE` | `/tickets/{id}` | Yes | Cancel ticket |

### Authorization

All protected endpoints require:
```
Authorization: Bearer <Firebase ID Token>
```

### Example Requests

```bash
# Verify token
curl -X POST http://localhost:8080/auth/verify \
  -H "Content-Type: application/json" \
  -d '{"idToken": "<firebase-id-token>"}'

# Get routes
curl http://localhost:8080/routes \
  -H "Authorization: Bearer <firebase-id-token>"

# Buy ticket
curl -X POST http://localhost:8080/tickets/buy \
  -H "Authorization: Bearer <firebase-id-token>" \
  -H "Content-Type: application/json" \
  -d '{"routeId": 1, "seatCount": 2}'

# Cancel ticket
curl -X DELETE http://localhost:8080/tickets/5 \
  -H "Authorization: Bearer <firebase-id-token>"
```

## Database Schema

Tables are created automatically via Flyway migration on first run:
- `users` — Firebase users
- `routes` — transport routes with seat availability
- `tickets` — purchased tickets with status
