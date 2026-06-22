# Newzic Frontend

Angular single-page application for the Newzic music platform. Features a dark-themed, music-centric UI with an integrated audio player, infinite scrolling marquees, and personalized recommendations.

## Tech Stack

| Category | Technology |
|----------|-----------|
| Framework | **Angular 20** (standalone components, signals, new control flow) |
| Language | **TypeScript 5** |
| Styling | **SCSS** with custom variables and mixins |
| HTTP | **HttpClient** with JWT interceptor |
| State | **Angular signals** (no external state management) |
| Build | **Angular CLI** / **esbuild** |
| Proxy / Serve | **Nginx** (production), `ng serve` (development) |
| Containerization | **Docker** (multi-stage: build + Nginx) |

## Project Structure

```
newzic-fe/
├── src/app/
│   ├── core/
│   │   ├── models/          # TypeScript interfaces (Song, Artist, Album, etc.)
│   │   ├── services/        # Singleton services injected app-wide
│   │   │   ├── auth.service.ts        # JWT auth, login/register, user state
│   │   │   ├── player.service.ts      # Audio player (HTML5 Audio, queue, shuffle, repeat)
│   │   │   ├── song.service.ts        # CRUD songs, reactions, play recording
│   │   │   ├── artist.service.ts      # Artist data, follow, recommendations
│   │   │   ├── feed.service.ts        # Social feed
│   │   │   ├── notification.service.ts
│   │   │   ├── stats.service.ts
│   │   │   ├── spotlight.service.ts
│   │   │   ├── album.service.ts
│   │   │   ├── collaboration.service.ts
│   │   │   └── i18n.service.ts
│   │   └── guards/          # Auth guard for protected routes
│   ├── features/
│   │   ├── home/            # Landing page (spotlight, trending, fresh drops, recommendations)
│   │   ├── auth/
│   │   │   ├── login/       # Login form
│   │   │   └── register/    # Multi-step registration
│   │   ├── artist/          # Artist profile page (music, about, photos tabs)
│   │   ├── profile/         # Current user's dashboard (stats, settings, avatar upload)
│   │   ├── upload/          # Song upload (audio + cover art, base64)
│   │   ├── search/          # Search artists & songs
│   │   ├── feed/            # Social feed page
│   │   ├── song/            # Song detail page
│   │   ├── collabs/         # Collaboration board
│   │   └── workspace/
│   │       ├── workspace-list/    # Workspace listing with filters & create modal
│   │       └── workspace-detail/  # Workspace detail (waveform player, comments, versions, files, chat)
│   ├── layout/
│   │   ├── navbar/          # Top navigation bar
│   │   ├── player/          # Bottom audio player bar
│   │   └── chat-widget/     # Floating chat widget
│   └── shared/
│       ├── components/      # Reusable components (Logo, FollowersModal, etc.)
│       ├── pipes/           # FormatNumber, Duration, Translate pipes
│       └── styles/          # SCSS variables, mixins, global styles
├── src/assets/i18n/         # Translation files (en, it, de, es)
├── nginx.conf               # Production Nginx config (SPA routing + API proxy)
├── Dockerfile               # Multi-stage build (Node → Nginx)
└── angular.json
```

## Key Features

### Audio Player
`PlayerService` wraps the HTML5 `Audio` API and provides:
- Play/pause, next/previous, shuffle, repeat (off/all/one)
- Progress tracking with seek support
- Volume control
- Queue management
- Fallback progress simulation when no audio URL is available
- Automatic play count recording via `SongService.recordPlay()`

### Authentication
- JWT stored in `localStorage`
- `HttpInterceptor` attaches `Authorization: Bearer <token>` to all API requests
- `AuthGuard` protects routes like `/profile`, `/upload`
- User state managed via signals in `AuthService`

### Multi-step Registration
The registration form is split into two steps:
1. **Account info** — artist name, username, email, password, role selection
2. **Preferences** — country, favorite genres

Both steps include **legal disclaimer checkboxes** (Terms of Service, Privacy Policy, age confirmation) that must be accepted before submission.

### Profile & Avatar Upload
- Avatar is clickable with a camera icon overlay
- Image is read as base64 and saved via `PATCH /api/users/me`
- Preferences (country, genres) are editable in the Settings tab

### Recommendations
- Personalized song and artist recommendations shown in the "For You" section
- Songs scroll via infinite marquee animation
- Artists displayed in a single row (overflow hidden)

### Reactions
Songs have four reaction types instead of likes:
- 🔥 Fire, 💎 Gem, 🎯 On Point, 🌟 Star
- Clickable buttons on artist page track rows
- Toggle on/off via `POST /api/songs/{id}/react`

### Legal Disclaimers
Both the registration and upload forms include required disclaimer checkboxes:
- **Registration** — Accept Terms of Service & Privacy Policy, confirm age ≥ 13
- **Upload** — Confirm content ownership/rights, accept Content Guidelines

Checkboxes are custom-styled with animated fill and validation is enforced before submission.

### Collaboration Workspaces
Full-featured workspace system for collaborative music production:
- **Workspace list** — filter by status (Draft, In Progress, Review, Done), create via modal
- **Workspace detail** — waveform audio player with timestamped comments, version management, file sharing, real-time chat
- **Version upload** — drag & drop or click-to-browse file picker (MP3, WAV, FLAC), same UX as the main upload page
- **Collaboration board** — browse and respond to collaboration requests, start workspaces directly

### Internationalization (i18n)
Full multi-language support via `I18nService` and `TranslatePipe`:
- **Supported languages**: English, Italian, German, Spanish
- **Translation files**: `src/assets/i18n/{en,it,de,es}.json`
- **Coverage**: navbar, registration, upload, collabs, workspaces, and all UI labels
- Language switcher in the navbar with flag icons

### Infinite Marquees
Trending artists, fresh drops, and recommended songs use CSS `@keyframes` marquee animations with 4x duplicated content for seamless infinite scrolling.

### Consistent Layout
All pages use a global `.container` class (`max-width: 1320px`, centered with horizontal padding) for consistent content width across the application.

## Development

### Prerequisites
- Node.js 18+
- Angular CLI 20+

### Install & run
```bash
npm install
ng serve
```
App runs at `http://localhost:4200`. API calls are expected at `http://localhost:8080/api`.

### Build for production
```bash
ng build --configuration=production
```
Output goes to `dist/newzic/`.

### Environment config
- `src/environments/environment.ts` — development (`apiUrl: 'http://localhost:8080/api'`)
- `src/environments/environment.prod.ts` — production (`apiUrl: '/api'`)

## Nginx Configuration

In production, Nginx serves the Angular bundle and proxies API calls:

```nginx
location / {
    root   /usr/share/nginx/html;
    try_files $uri $uri/ /index.html;    # SPA fallback
}

location /api/ {
    client_max_body_size 50M;            # For base64 audio uploads
    proxy_pass http://backend:8080/api/;
}
```

## Styling

- **Dark theme** with SCSS variables (`$bg-primary`, `$accent-violet`, `$accent-blue`, etc.)
- **Glass morphism** via `@mixin glass()` for card backgrounds
- **Responsive** via `@mixin mobile` media query breakpoint
- Custom component-scoped SCSS per feature
