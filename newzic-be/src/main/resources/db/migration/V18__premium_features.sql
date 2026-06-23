-- =============================================
-- V18: Premium features
--   - Premium subscription fields on users
--   - Donation system ("Supporta l'Artista")
-- =============================================

-- ── Premium fields on users ──
ALTER TABLE users
    ADD COLUMN premium_since   TIMESTAMP,
    ADD COLUMN custom_url      VARCHAR(100) UNIQUE;

-- ── Donations ──
CREATE TABLE donations (
    id              UUID PRIMARY KEY,
    from_user_id    UUID           NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    to_artist_id    UUID           NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    amount_cents    INT            NOT NULL,
    artist_cents    INT            NOT NULL,
    platform_cents  INT            NOT NULL,
    message         TEXT,
    created_at      TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_donation_from    ON donations(from_user_id);
CREATE INDEX idx_donation_to      ON donations(to_artist_id, created_at DESC);
