# Newzic Backend

Spring Boot REST API powering the Newzic music platform. Handles authentication, song management, artist profiles, social features, recommendations, and analytics.

## Tech Stack

| Category | Technology |
|----------|-----------|
| Language | **Kotlin 1.9** |
| Framework | **Spring Boot 3.5** (Web, Security, Data JPA, Validation) |
| Database | **PostgreSQL 16** |
| Migrations | **Flyway** (versioned SQL scripts in `src/main/resources/db/migration/`) |
| Auth | **JWT** (HMAC-SHA256) + **BCrypt** password hashing |
| Build | **Gradle 8.8** (Kotlin DSL) |
| Runtime | **JDK 21** |
| Containerization | **Docker** (multi-stage build) |

## Project Structure

```
newzic-be/
├── src/main/kotlin/com/newzic/
│   ├── api/
│   │   ├── controller/     # REST controllers (UserController, SongController, etc.)
│   │   ├── dto/            # Request/Response data classes
│   │   └── mapper/         # Entity ↔ DTO mappers
│   ├── config/             # Security, JWT, CORS configuration
│   ├── domain/
│   │   ├── entity/         # JPA entities (UserEntity, SongEntity, etc.)
│   │   └── repository/     # Spring Data JPA repositories
│   └── service/            # Business logic (SongService, UserService, etc.)
├── src/main/resources/
│   ├── application.yml     # App configuration
│   └── db/migration/       # Flyway SQL migrations (V1 through V10)
├── Dockerfile
└── build.gradle.kts
```

## Key Design Decisions

- **Base64 audio storage**: Songs store audio as base64 data URIs in a `TEXT` column. This simplifies deployment (no external object storage needed) at the cost of database size. Suitable for demo/MVP stage.
- **Reaction system**: Instead of a single "like", songs have four distinct reactions (`fire`, `gem`, `onpoint`, `star`) stored both as individual `ReactionEntity` records and as denormalized counters on `SongEntity`.
- **Recommendation engine**: `SongService.getRecommended` and `UserService.getRecommended` score artists/songs based on genre overlap, country match, and engagement metrics. Pure algorithmic, no ML.
- **Follow toggle**: `POST /artists/{id}/follow` is idempotent — it follows if not following, unfollows if already following. Returns the new state.
- **`@Transactional` on reads**: Methods accessing lazy-loaded collections (e.g. `artist.genres`) use `@Transactional(readOnly = true)` to keep the Hibernate session open.

## Prerequisites

- JDK 21
- PostgreSQL 15+
- Gradle 8.8+ (or use the included `./gradlew` wrapper)

## Setup

### 1. Create the database
```bash
createdb newzic
# or via psql:
# CREATE DATABASE newzic;
# CREATE USER newzic WITH PASSWORD 'newzic';
# GRANT ALL PRIVILEGES ON DATABASE newzic TO newzic;
```

### 2. Configure environment (optional)
Defaults are in `application.yml`. Override with env vars:
```bash
export DB_USERNAME=newzic
export DB_PASSWORD=newzic
export JWT_SECRET=your-secret-key-minimum-256-bits
```

### 3. Run
```bash
./gradlew bootRun
```
Server starts on `http://localhost:8080`.

### 4. Build for production
```bash
./gradlew bootJar -x test
# Output: build/libs/newzic-be-0.0.1-SNAPSHOT.jar
```

## Database Migrations

Flyway runs automatically on startup. Migrations are numbered sequentially:

| Version | Description |
|---------|-------------|
| V1 | Initial schema (users, songs, albums) |
| V2–V8 | Feed, notifications, reactions, follows, spotlight, seed data |
| V9 | Enlarge `audio_url` column to TEXT |
| V10 | Enlarge user `avatar` and `cover` columns to TEXT |

## API Endpoints

### Auth
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/register` | — | Register new user |
| POST | `/api/auth/login` | — | Login, returns JWT |

### Users / Artists
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/users/me` | ✅ | Current user profile |
| PATCH | `/api/users/me` | ✅ | Update profile (name, avatar, bio, genres, etc.) |
| GET | `/api/artists/{id}` | — | Get artist by ID |
| GET | `/api/artists/trending?size=` | — | Trending artists (paginated) |
| GET | `/api/artists/producers?size=` | — | Top producers (paginated) |
| GET | `/api/artists/community-picks?size=` | — | Community picks (paginated) |
| GET | `/api/artists/recommended?size=` | ✅ | Personalized artist recommendations |
| GET | `/api/artists/search?q=` | — | Search artists by name |
| POST | `/api/artists/{id}/follow` | ✅ | Toggle follow/unfollow |
| GET | `/api/artists/{id}/following` | ✅ | Check if current user follows artist |

### Songs
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/songs/{id}` | — | Get song by ID |
| GET | `/api/songs/trending?size=` | — | Trending songs (paginated) |
| GET | `/api/songs/new-releases?size=` | — | New releases (paginated) |
| GET | `/api/songs/recommended?size=` | ✅ | Personalized song recommendations |
| GET | `/api/songs/artist/{artistId}` | — | Songs by a specific artist |
| GET | `/api/songs/search?q=` | — | Search songs by title |
| POST | `/api/songs` | ✅ | Create/upload a song (with optional base64 audio) |
| POST | `/api/songs/{id}/react?type=` | ✅ | Toggle reaction (`fire`/`gem`/`onpoint`/`star`) |
| POST | `/api/songs/{id}/play` | — | Record a play event |

### Feed
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/feed` | — | Global feed |
| GET | `/api/feed/user/{userId}` | — | User's feed posts |
| POST | `/api/feed` | ✅ | Create feed post |

### Notifications
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/notifications` | ✅ | User notifications |
| GET | `/api/notifications/unread-count` | ✅ | Unread count |
| POST | `/api/notifications/mark-read` | ✅ | Mark all as read |

### Stats & Spotlight
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/stats/me` | ✅ | Current user stats |
| GET | `/api/stats/artist/{id}` | — | Artist stats |
| GET | `/api/spotlight/current` | — | Current week spotlight |

## Configuration Reference

Key settings in `application.yml`:

```yaml
server:
  port: 8080
  tomcat:
    max-http-form-post-size: 50MB   # For large JSON payloads (base64 audio)
    max-swallow-size: 50MB

spring:
  servlet:
    multipart:
      max-file-size: 50MB
      max-request-size: 50MB

jwt:
  secret: ${JWT_SECRET}
  expiration: 604800000   # 7 days in milliseconds
```
