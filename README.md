# 🎵 Newzic

**Newzic** is a music platform built for independent artists, producers, and beatmakers who want to share their sound with the world. Think of it as a social-first music hub where emerging talent can upload tracks, build a fanbase, discover new artists, and connect with like-minded creators — all in one place.

The platform is designed around the idea that every artist deserves a stage. Whether you're a bedroom producer dropping lo-fi beats, a singer-songwriter sharing acoustic demos, or a band looking for collaborators, Newzic gives you the tools to grow your audience organically.

## What you can do on Newzic

- **Upload & share music** — publish tracks with cover art, genres, and tags
- **Discover new artists** — personalized recommendations based on your country, taste, and listening habits
- **React to tracks** — fire 🔥, gem 💎, on point 🎯, and star 🌟 reactions instead of generic likes
- **Follow artists** — build your network and stay updated on new releases
- **Artist profiles** — showcase your music, bio, photos, social links, and collaboration availability
- **Trending & charts** — see what's hot in the community right now
- **Feed (Open Mic)** — a social feed where artists share updates, thoughts, and previews
- **Weekly Spotlight** — curated editorial picks highlighting standout artists
- **Producer & Beatmaker section** — dedicated space for the behind-the-scenes creators
- **Fresh Drops** — continuous stream of newly released tracks
- **Mobile-ready** — native Android & iOS apps via Capacitor

## Architecture

The project is a monorepo with three sub-projects:

```
newzic/
├── newzic-be/     → Backend API (Kotlin + Spring Boot)
├── newzic-fe/     → Web Frontend (Angular 20)
├── newzic-app/    → Native mobile wrapper (Capacitor)
├── docker-compose.yml
└── start.sh
```

| Layer | Tech | Description |
|-------|------|-------------|
| **Backend** | Kotlin, Spring Boot 3.5, PostgreSQL, Flyway, JWT | REST API handling auth, songs, artists, feed, stats, recommendations |
| **Frontend** | Angular 20, SCSS, standalone components, signals | SPA with dark-themed music UI, player bar, infinite marquees |
| **Mobile** | Capacitor, Android, iOS | Wraps the Angular build into native apps |
| **Infrastructure** | Docker/Podman Compose, Nginx | PostgreSQL + backend + frontend containers |

## Quick Start

### Prerequisites

- **JDK 21**
- **Node.js 18+**
- **Podman** or **Docker** (with Compose)
- **Gradle 8.8+** (or use the included wrapper)

### Run everything with one command

```bash
./start.sh
```

This will:
1. Build the Spring Boot backend (`bootJar`)
2. Build the Angular frontend (`ng build --production`)
3. Start all containers via Podman Compose (PostgreSQL, backend, frontend)

Once running:
- **Frontend**: [http://localhost:4200](http://localhost:4200)
- **Backend API**: [http://localhost:8080/api](http://localhost:8080/api)
- **Database**: `localhost:5432` (user: `newzic`, password: `newzic`)

### Run individually (development)

```bash
# Backend
cd newzic-be && ./gradlew bootRun

# Frontend
cd newzic-fe && npm install && ng serve

# Mobile (after building frontend)
cd newzic-app && npm run sync && npm run open:android
```

### Run tests

```bash
# Backend (Kotlin/JUnit 5 + Mockito)
cd newzic-be && ./gradlew test

# Frontend (Karma + Jasmine, headless Chrome)
cd newzic-fe && npx ng test --watch=false

# Run both at once
cd newzic-be && ./gradlew test && cd ../newzic-fe && npx ng test --watch=false
```

## Key Features

### Music & Discovery
- **Personalized recommendations** — matching algorithm based on country/region, genres, listening preferences, popularity, and verified status
- **Reactions system** — fire 🔥, gem 💎, on point 🎯, star 🌟 (beyond simple likes)
- **Song likes** — like/unlike songs from the player bar or song cards with animated heart icon
- **Trending & Fresh Drops** — auto-generated charts and new release streams
- **Premium Discovery boost** — premium artists get moderate visibility boost in recommendations (quality still matters)

### Social
- **Follow/unfollow** with real-time follower/following counts
- **Followers & Following modal** — click on follower/following counts to see the full list and navigate to profiles
- **Real-time messaging** — chat widget with unread badge, conversation list, and message history
- **Open Mic feed** — social feed for artist updates and previews
- **Weekly Spotlight** — editorial curated picks

### Workspace Collaboration
- **Real-time workspaces** — create collaborative projects with other artists
- **Version management** — upload and compare audio versions with timestamped comments
- **Activity feed** — vertical timeline with colored icons, user avatars, and formatted messages
- **Tasks & files** — organize production with tasks, shared files, and reference tracks
- **Chat** — workspace-scoped messaging for project discussions

### Newzic Premium (€9.99/month)
- **Unlimited songs** — free plan limited to 10 published tracks
- **Unlimited workspaces** — free plan limited to 1 workspace
- **Unlimited collaborators** — free plan limited to 4 per workspace
- **Unlimited comments** — free plan limited to 5 per version
- **Premium badge** — animated gradient badge on profile and cards
- **Verified profile** — checkmark badge next to artist name
- **Custom URL** — personalized profile URL (e.g., newzic.com/your-name)
- **Advanced analytics** — audience demographics, song performance, growth trends
- **Discovery boost** — moderate ranking boost in recommendations
- **Upgrade paywall** — beautiful modal shown when free limits are reached
- **Logo branding** — "NEWZIC PREMIUM" shown in navbar for premium users

### Supporta l'Artista (Donation System)
- **Transparent donations** — 95% goes to the artist, 5% platform fee
- **Preset & custom amounts** — quick-pick buttons (€1, €3, €5, €10, €25) or custom input
- **Personal messages** — attach a message to your donation
- **Donation dashboard** — total received, supporter count, top supporters, recent donations
- **Fee breakdown** — real-time transparency showing artist vs. platform split

### Profiles
- **Artist profiles** — music, about, photos tabs with social links, collaboration availability, and genre tags
- **Premium & verified badges** — reusable components with gradient styling
- **User profile dashboard** — stats (plays, followers, reactions, tracks, following), liked songs, settings
- **Clickable follower/following stats** on both profile and artist pages

### Advanced Analytics (Premium)
- **Audience demographics** — country, gender, age distribution
- **Audience interests** — genre affinities from play data
- **Song performance** — plays, likes, shares, saves, new followers per track
- **Growth metrics** — daily, weekly, monthly play/follower trends with direction indicators
- **Donation overview** — integrated into analytics dashboard

### Internationalization
- **4 languages**: English, Italian, German, Spanish
- Language preference saved per-user (synced to backend)
- All UI strings use translation keys via `TranslatePipe` (supports parameter interpolation)
- Translation files: `src/assets/i18n/{en,it,de,es}.json`

## Sub-projects

Each sub-project has its own detailed README:

- [`newzic-be/README.md`](newzic-be/README.md) — Backend API docs, endpoints, database setup
- [`newzic-fe/README.md`](newzic-fe/README.md) — Frontend architecture, components, styling
- [`newzic-app/README.md`](newzic-app/README.md) — Mobile app setup, build & release workflow

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_USERNAME` | `newzic` | PostgreSQL username |
| `DB_PASSWORD` | `newzic` | PostgreSQL password |
| `JWT_SECRET` | (dev default) | JWT signing key (min 256 bits, **change in production**) |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://db:5432/newzic` | Database connection URL |

## Deploy to Railway

See [`DEPLOY.md`](DEPLOY.md) for step-by-step Railway deployment instructions.

The project is pre-configured with:
- Multi-stage Dockerfiles (build + runtime in a single image)
- `railway.toml` per service
- Environment-driven configuration (DB, JWT, CORS, backend URL)
- Railway internal networking for frontend → backend proxy

## License

This project is proprietary. All rights reserved.
