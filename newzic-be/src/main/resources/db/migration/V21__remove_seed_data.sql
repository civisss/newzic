-- ═══════════════════════════════════════════════════════════
-- V21: Remove all seed/mock data for production readiness
-- The platform starts empty; content is user-generated only.
-- ═══════════════════════════════════════════════════════════

-- Journal data (depends on posts)
DELETE FROM journal_comments;
DELETE FROM journal_reactions;
DELETE FROM journal_post_tagged_users;
DELETE FROM journal_post_hashtags;
DELETE FROM journal_posts;

-- Feed
DELETE FROM feed_posts;

-- Collaborations
DELETE FROM collab_tags;
DELETE FROM collab_genres;
DELETE FROM collaborations;

-- Spotlights
DELETE FROM spotlights;

-- Song-related
DELETE FROM song_likes;
DELETE FROM reactions;
DELETE FROM play_events;
DELETE FROM song_tags;
DELETE FROM songs;

-- Albums
DELETE FROM albums;

-- Messages
DELETE FROM messages;

-- Workspaces
DELETE FROM workspace_chat_messages;
DELETE FROM workspace_files;
DELETE FROM workspace_comments;
DELETE FROM workspace_versions;
DELETE FROM workspace_members;
DELETE FROM workspaces;

-- Notifications
DELETE FROM notifications;

-- Follows
DELETE FROM follows;

-- User photos
DELETE FROM user_photos;

-- User data (keep admin for initial access)
DELETE FROM user_preferred_genres WHERE user_id != 'a1000000-0000-0000-0000-000000000099';
DELETE FROM user_tags WHERE user_id != 'a1000000-0000-0000-0000-000000000099';
DELETE FROM user_genres WHERE user_id != 'a1000000-0000-0000-0000-000000000099';
DELETE FROM user_roles WHERE user_id != 'a1000000-0000-0000-0000-000000000099';
DELETE FROM users WHERE id != 'a1000000-0000-0000-0000-000000000099';

-- Reset admin user stats to zero and remove fake avatar/cover
UPDATE users SET
    avatar = NULL,
    cover = NULL,
    followers = 0,
    following = 0,
    total_plays = 0,
    verified = false,
    weekly_growth = 0
WHERE id = 'a1000000-0000-0000-0000-000000000099';
