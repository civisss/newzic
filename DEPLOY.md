# Deploying Newzic on Railway

This guide covers deploying all three services (PostgreSQL, Backend, Frontend) on [Railway](https://railway.app).

## Architecture on Railway

```
┌─────────────────────────────────────────────────┐
│  Railway Project: newzic                        │
│                                                 │
│  ┌──────────┐   ┌──────────┐   ┌──────────┐   │
│  │ PostgreSQL│◄──│ Backend  │◄──│ Frontend │   │
│  │ (plugin)  │   │ (newzic- │   │ (newzic- │   │
│  │           │   │  be)     │   │  fe)     │   │
│  └──────────┘   └──────────┘   └──────────┘   │
│                                  ▲ public URL   │
│                                  │              │
│                            users connect here   │
└─────────────────────────────────────────────────┘
```

## Step-by-step setup

### 1. Create a new Railway project

Go to [railway.app/new](https://railway.app/new) and create an empty project.

### 2. Add PostgreSQL

- Click **"+ New"** → **"Database"** → **"PostgreSQL"**
- Railway provisions it automatically
- Note the connection variables (they'll be available as env vars)

### 3. Deploy the Backend

- Click **"+ New"** → **"GitHub Repo"** → select your `newzic` repo
- Set **Root Directory** to `newzic-be`
- Railway detects the `Dockerfile` automatically

**Set these environment variables** on the backend service:

| Variable | Value |
|----------|-------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}` |
| `DB_USERNAME` | `${{Postgres.PGUSER}}` |
| `DB_PASSWORD` | `${{Postgres.PGPASSWORD}}` |
| `JWT_SECRET` | Generate a random 64-char string (e.g. `openssl rand -hex 32`) |
| `CORS_ORIGINS` | `https://your-frontend.up.railway.app` (set after frontend deploys) |
| `PORT` | `8080` |

> **Tip**: Railway supports `${{ServiceName.VAR}}` syntax to reference variables from other services.

### 4. Deploy the Frontend

- Click **"+ New"** → **"GitHub Repo"** → select your `newzic` repo again
- Set **Root Directory** to `newzic-fe`
- Railway detects the `Dockerfile` automatically

**Set these environment variables** on the frontend service:

| Variable | Value |
|----------|-------|
| `BACKEND_URL` | `http://newzic-be.railway.internal:8080` |
| `PORT` | `80` |

> **Note**: `*.railway.internal` is Railway's private networking. The frontend Nginx proxies `/api/` calls to the backend internally — no public backend URL needed.

### 5. Generate a public domain

- On the **frontend** service, go to **Settings** → **Networking** → **Generate Domain**
- You'll get a URL like `newzic-fe-production.up.railway.app`
- Go back to the **backend** service and update `CORS_ORIGINS` to include this URL

### 6. Custom domain (optional)

- On the frontend service: **Settings** → **Networking** → **Custom Domain**
- Add your domain (e.g. `app.newzic.com`)
- Update `CORS_ORIGINS` on the backend to include it

## Environment Variables Reference

### Backend (`newzic-be`)

| Variable | Required | Description |
|----------|----------|-------------|
| `SPRING_DATASOURCE_URL` | ✅ | JDBC connection string to PostgreSQL |
| `DB_USERNAME` | ✅ | Database username |
| `DB_PASSWORD` | ✅ | Database password |
| `JWT_SECRET` | ✅ | JWT signing key (min 256 bits / 32 chars) |
| `CORS_ORIGINS` | ✅ | Comma-separated allowed origins (e.g. `https://app.newzic.com,https://newzic-fe.up.railway.app`) |
| `PORT` | — | Server port (default: `8080`) |

### Frontend (`newzic-fe`)

| Variable | Required | Description |
|----------|----------|-------------|
| `BACKEND_URL` | ✅ | Internal backend URL for Nginx proxy (e.g. `http://newzic-be.railway.internal:8080`) |
| `PORT` | — | Nginx listen port (default: `80`, Railway may override) |

## Running locally with the same config

The setup works identically on your local machine via Docker/Podman Compose:

```bash
# Option 1: One-command start (builds + runs everything)
./start.sh

# Option 2: Just containers (if you already built)
podman compose up --build

# Option 3: Individual services for development
cd newzic-be && ./gradlew bootRun
cd newzic-fe && npm install && ng serve
```

Local defaults are already configured in `application.yml` — no env vars needed for development.

## Troubleshooting

### Backend won't start
- Check that PostgreSQL is healthy before the backend deploys
- Verify `SPRING_DATASOURCE_URL` uses the correct Railway Postgres reference
- Check Railway deploy logs for Flyway migration errors

### CORS errors in browser
- Make sure `CORS_ORIGINS` on the backend includes the exact frontend URL (with `https://`)
- Multiple origins: separate with commas (no spaces)

### 413 Request Entity Too Large
- The Nginx config already sets `client_max_body_size 50M`
- If Railway's proxy has a lower limit, check their docs for request size settings

### Frontend shows blank page
- Verify the `BACKEND_URL` env var is set on the frontend service
- Check that the backend service name matches what's in the URL
- Railway internal networking requires both services to be in the same project
