-- =============================================
-- V14: Indexes to support recommendation engine
-- =============================================

-- Faster lookup of songs by genre + not-own-artist
CREATE INDEX IF NOT EXISTS idx_songs_genre_artist ON songs(genre, artist_id);

-- Faster lookup of reactions by user (to build taste profile)
CREATE INDEX IF NOT EXISTS idx_reactions_user_created ON reactions(user_id, created_at DESC);

-- Faster lookup of song_likes by user (to build taste profile)
CREATE INDEX IF NOT EXISTS idx_song_likes_user ON song_likes(user_id, created_at DESC);

-- Faster lookup of play_events by user (to build listen history)
CREATE INDEX IF NOT EXISTS idx_play_events_user_created ON play_events(user_id, created_at DESC);

-- Composite index for feed personalization: posts by author ordered by time
CREATE INDEX IF NOT EXISTS idx_feed_author_created ON feed_posts(author_id, created_at DESC);

-- Index on user country for geo-based recommendations
CREATE INDEX IF NOT EXISTS idx_users_country ON users(country);
