-- Add admin demo user with password 'admin'
-- BCrypt hash for 'admin': $2a$10$cVrdH90/PBJL9DpvC.biHOEyBTk46v6amVcIcKl4bSG3WkL.cOSnC
INSERT INTO users (id, username, email, password_hash, display_name, avatar, cover, bio, long_bio, followers, following, total_plays, verified, premium, location, looking_for_collab, weekly_growth)
VALUES (
    'a1000000-0000-0000-0000-000000000099',
    'admin',
    'admin@newzic.com',
    '$2a$10$cVrdH90/PBJL9DpvC.biHOEyBTk46v6amVcIcKl4bSG3WkL.cOSnC',
    'Demo Artist',
    'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200&h=200&fit=crop&crop=face',
    'https://images.unsplash.com/photo-1557683316-973673baf926?w=1200&h=400&fit=crop',
    'Welcome to Newzic! This is a demo account.',
    'This is the demo admin account for exploring the Newzic platform.',
    0, 0, 0, false, false,
    NULL, false, 0
);

INSERT INTO user_roles (user_id, role) VALUES ('a1000000-0000-0000-0000-000000000099', 'SINGER');
