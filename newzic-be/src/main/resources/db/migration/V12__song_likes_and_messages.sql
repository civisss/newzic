-- =============================================
-- V12: Song likes + Messages + User preferred language
-- =============================================

-- Song Likes (user likes a song → "songs I like")
CREATE TABLE song_likes (
    id          UUID PRIMARY KEY,
    user_id     UUID      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    song_id     UUID      NOT NULL REFERENCES songs(id) ON DELETE CASCADE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, song_id)
);

CREATE INDEX idx_song_likes_user ON song_likes(user_id, created_at DESC);
CREATE INDEX idx_song_likes_song ON song_likes(song_id);

-- Messages
CREATE TABLE messages (
    id           UUID PRIMARY KEY,
    sender_id    UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    recipient_id UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content      TEXT         NOT NULL,
    is_read      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_messages_sender    ON messages(sender_id, created_at DESC);
CREATE INDEX idx_messages_recipient ON messages(recipient_id, created_at DESC);
CREATE INDEX idx_messages_conversation ON messages(
    LEAST(sender_id, recipient_id),
    GREATEST(sender_id, recipient_id),
    created_at DESC
);

-- User preferred language
ALTER TABLE users ADD COLUMN preferred_language VARCHAR(5) DEFAULT 'en';
