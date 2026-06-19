-- =============================================
-- V1: Initial schema for Newzic
-- =============================================

-- Users
CREATE TABLE users (
    id              UUID PRIMARY KEY,
    username        VARCHAR(30)  NOT NULL UNIQUE,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    display_name    VARCHAR(100) NOT NULL,
    avatar          VARCHAR(500),
    cover           VARCHAR(500),
    bio             TEXT,
    long_bio        TEXT,
    followers       INT          NOT NULL DEFAULT 0,
    following       INT          NOT NULL DEFAULT 0,
    total_plays     BIGINT       NOT NULL DEFAULT 0,
    verified        BOOLEAN      NOT NULL DEFAULT FALSE,
    location        VARCHAR(100),
    looking_for_collab BOOLEAN   NOT NULL DEFAULT FALSE,
    collab_description TEXT,
    weekly_growth   DOUBLE PRECISION,
    spotify_url     VARCHAR(500),
    youtube_music_url VARCHAR(500),
    apple_music_url VARCHAR(500),
    soundcloud_url  VARCHAR(500),
    tiktok_url      VARCHAR(500),
    instagram_url   VARCHAR(500),
    joined_date     DATE         NOT NULL DEFAULT CURRENT_DATE,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role    VARCHAR(20) NOT NULL,
    PRIMARY KEY (user_id, role)
);

CREATE TABLE user_genres (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    genre   VARCHAR(50) NOT NULL
);

CREATE TABLE user_tags (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    tag     VARCHAR(50) NOT NULL
);

CREATE TABLE user_photos (
    user_id   UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    photo_url VARCHAR(500) NOT NULL
);

-- Albums
CREATE TABLE albums (
    id           UUID PRIMARY KEY,
    title        VARCHAR(200) NOT NULL,
    artist_id    UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    cover        VARCHAR(500),
    type         VARCHAR(10)  NOT NULL DEFAULT 'ALBUM',
    release_date DATE         NOT NULL DEFAULT CURRENT_DATE,
    genre        VARCHAR(50),
    total_plays  BIGINT       NOT NULL DEFAULT 0,
    description  TEXT,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Songs
CREATE TABLE songs (
    id                 UUID PRIMARY KEY,
    title              VARCHAR(200) NOT NULL,
    artist_id          UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    album_id           UUID         REFERENCES albums(id) ON DELETE SET NULL,
    cover              VARCHAR(500),
    duration           INT          NOT NULL DEFAULT 0,
    genre              VARCHAR(50),
    release_date       DATE         NOT NULL DEFAULT CURRENT_DATE,
    plays              BIGINT       NOT NULL DEFAULT 0,
    likes              BIGINT       NOT NULL DEFAULT 0,
    audio_url          VARCHAR(500),
    is_explicit        BOOLEAN      NOT NULL DEFAULT FALSE,
    reactions_fire     BIGINT       NOT NULL DEFAULT 0,
    reactions_gem      BIGINT       NOT NULL DEFAULT 0,
    reactions_onpoint  BIGINT       NOT NULL DEFAULT 0,
    reactions_star     BIGINT       NOT NULL DEFAULT 0,
    track_order        INT,
    created_at         TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE song_tags (
    song_id UUID NOT NULL REFERENCES songs(id) ON DELETE CASCADE,
    tag     VARCHAR(50) NOT NULL
);

-- Reactions
CREATE TABLE reactions (
    id         UUID PRIMARY KEY,
    user_id    UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    song_id    UUID        NOT NULL REFERENCES songs(id) ON DELETE CASCADE,
    type       VARCHAR(10) NOT NULL,
    created_at TIMESTAMP   NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, song_id, type)
);

-- Follows
CREATE TABLE follows (
    id           UUID PRIMARY KEY,
    follower_id  UUID      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    following_id UUID      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (follower_id, following_id)
);

-- Feed Posts
CREATE TABLE feed_posts (
    id                 UUID PRIMARY KEY,
    type               VARCHAR(30)  NOT NULL,
    author_id          UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content            TEXT         NOT NULL,
    image              VARCHAR(500),
    song_id            UUID         REFERENCES songs(id) ON DELETE SET NULL,
    likes              BIGINT       NOT NULL DEFAULT 0,
    comments           BIGINT       NOT NULL DEFAULT 0,
    reactions_fire     BIGINT       NOT NULL DEFAULT 0,
    reactions_gem      BIGINT       NOT NULL DEFAULT 0,
    reactions_onpoint  BIGINT       NOT NULL DEFAULT 0,
    reactions_star     BIGINT       NOT NULL DEFAULT 0,
    created_at         TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Collaborations
CREATE TABLE collaborations (
    id          UUID PRIMARY KEY,
    title       VARCHAR(200) NOT NULL,
    description TEXT         NOT NULL,
    author_id   UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category    VARCHAR(20)  NOT NULL,
    status      VARCHAR(15)  NOT NULL DEFAULT 'OPEN',
    responses   INT          NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE collab_genres (
    collab_id UUID NOT NULL REFERENCES collaborations(id) ON DELETE CASCADE,
    genre     VARCHAR(50) NOT NULL
);

CREATE TABLE collab_tags (
    collab_id UUID NOT NULL REFERENCES collaborations(id) ON DELETE CASCADE,
    tag       VARCHAR(50) NOT NULL
);

-- Notifications
CREATE TABLE notifications (
    id           UUID PRIMARY KEY,
    type         VARCHAR(20)  NOT NULL,
    message      VARCHAR(500) NOT NULL,
    recipient_id UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    from_user_id UUID         REFERENCES users(id) ON DELETE SET NULL,
    link         VARCHAR(500),
    is_read      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Spotlights
CREATE TABLE spotlights (
    id               UUID PRIMARY KEY,
    artist_id        UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    quote            TEXT         NOT NULL,
    featured_song_id UUID         REFERENCES songs(id) ON DELETE SET NULL,
    editor_note      TEXT,
    week_label       VARCHAR(100) NOT NULL,
    active           BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Play Events (analytics)
CREATE TABLE play_events (
    id                UUID PRIMARY KEY,
    song_id           UUID      NOT NULL REFERENCES songs(id) ON DELETE CASCADE,
    user_id           UUID      REFERENCES users(id) ON DELETE SET NULL,
    city              VARCHAR(100),
    country           VARCHAR(100),
    duration_listened INT       NOT NULL DEFAULT 0,
    created_at        TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =============================================
-- Indexes
-- =============================================
CREATE INDEX idx_songs_artist      ON songs(artist_id);
CREATE INDEX idx_songs_album       ON songs(album_id);
CREATE INDEX idx_songs_plays       ON songs(plays DESC);
CREATE INDEX idx_songs_release     ON songs(release_date DESC);
CREATE INDEX idx_songs_genre       ON songs(genre);

CREATE INDEX idx_albums_artist     ON albums(artist_id);

CREATE INDEX idx_reactions_song    ON reactions(song_id);
CREATE INDEX idx_reactions_user    ON reactions(user_id);

CREATE INDEX idx_follows_follower  ON follows(follower_id);
CREATE INDEX idx_follows_following ON follows(following_id);

CREATE INDEX idx_feed_author       ON feed_posts(author_id);
CREATE INDEX idx_feed_created      ON feed_posts(created_at DESC);

CREATE INDEX idx_collabs_author    ON collaborations(author_id);
CREATE INDEX idx_collabs_status    ON collaborations(status);

CREATE INDEX idx_notif_recipient   ON notifications(recipient_id, created_at DESC);
CREATE INDEX idx_notif_unread      ON notifications(recipient_id, is_read) WHERE is_read = FALSE;

CREATE INDEX idx_play_events_song  ON play_events(song_id);
CREATE INDEX idx_play_events_user  ON play_events(user_id);
CREATE INDEX idx_play_events_time  ON play_events(created_at);

CREATE INDEX idx_users_followers   ON users(followers DESC);
CREATE INDEX idx_users_plays       ON users(total_plays DESC);
