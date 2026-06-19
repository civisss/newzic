-- =============================================
-- V2: Seed data — demo users, songs, feed, etc.
-- Password for all users: password123
-- BCrypt hash: $2a$10$dXJ3SW6G7P50lGmMQoeGbOwS3tY6bGHvUwEA3.LyH2rMd/1hSSfLO
-- =============================================

-- ── USERS ──
INSERT INTO users (id, username, email, password_hash, display_name, avatar, cover, bio, long_bio, followers, following, total_plays, verified, location, looking_for_collab, collab_description, weekly_growth, spotify_url, instagram_url, tiktok_url, soundcloud_url) VALUES
('a1000000-0000-0000-0000-000000000001', 'marcorossi', 'marco@newzic.com', '$2a$10$dXJ3SW6G7P50lGmMQoeGbOwS3tY6bGHvUwEA3.LyH2rMd/1hSSfLO', 'Marco Rossi', 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200&h=200&fit=crop&crop=face', 'https://images.unsplash.com/photo-1557683316-973673baf926?w=1200&h=400&fit=crop', 'Italian singer-songwriter blending pop and indie vibes.', 'Born in Milan, Marco started writing songs at 15. After years of performing in small venues across Italy, he found his voice in a mix of pop melodies and indie storytelling. His debut EP "Notti di Vetro" gained traction on Spotify and marked the beginning of a new chapter.', 45200, 120, 2150000, true, 'Milan, Italy', true, 'Looking for a producer for my next EP', 12.5, 'https://open.spotify.com/artist/marco', 'https://instagram.com/marcorossi', 'https://tiktok.com/@marcorossi', 'https://soundcloud.com/marcorossi'),

('a1000000-0000-0000-0000-000000000002', 'lunabeatss', 'luna@newzic.com', '$2a$10$dXJ3SW6G7P50lGmMQoeGbOwS3tY6bGHvUwEA3.LyH2rMd/1hSSfLO', 'Luna Vega', 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200&h=200&fit=crop&crop=face', 'https://images.unsplash.com/photo-1557683316-973673baf926?w=1200&h=400&fit=crop', 'Bedroom producer making dreamy lo-fi beats.', 'Luna started producing beats in her bedroom at 17 using nothing but a laptop and free plugins. Her lo-fi sound caught attention on YouTube and she quickly built a fanbase of chill music lovers worldwide.', 28700, 85, 1340000, false, 'Barcelona, Spain', true, 'Open to vocalists for lo-fi collabs', 6.8, 'https://open.spotify.com/artist/luna', 'https://instagram.com/lunabeatss', NULL, 'https://soundcloud.com/lunabeatss'),

('a1000000-0000-0000-0000-000000000003', 'drvgn', 'drvgn@newzic.com', '$2a$10$dXJ3SW6G7P50lGmMQoeGbOwS3tY6bGHvUwEA3.LyH2rMd/1hSSfLO', 'DRVGN', 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&h=200&fit=crop&crop=face', 'https://images.unsplash.com/photo-1557683316-973673baf926?w=1200&h=400&fit=crop', 'Electronic producer and DJ from Berlin.', 'DRVGN (pronounced Dragon) is an electronic music producer and DJ from Berlin. Known for his heavy bass drops and atmospheric builds, he has performed at clubs across Europe.', 31500, 42, 980000, true, 'Berlin, Germany', false, NULL, 15.2, 'https://open.spotify.com/artist/drvgn', 'https://instagram.com/drvgn', 'https://tiktok.com/@drvgn', NULL),

('a1000000-0000-0000-0000-000000000004', 'midnightcoll', 'midnight@newzic.com', '$2a$10$dXJ3SW6G7P50lGmMQoeGbOwS3tY6bGHvUwEA3.LyH2rMd/1hSSfLO', 'The Midnight Collective', 'https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=200&h=200&fit=crop&crop=face', 'https://images.unsplash.com/photo-1557683316-973673baf926?w=1200&h=400&fit=crop', 'Indie rock band from Manchester. Raw energy meets poetic lyricism.', 'Formed in a Manchester garage in 2021, The Midnight Collective blends raw punk energy with poetic lyrics. Every weekend, somewhere in the UK, they are on stage.', 32100, 15, 1870000, true, 'Manchester, UK', true, 'Looking for a keys player', 15.2, NULL, 'https://instagram.com/midnightcoll', NULL, NULL),

('a1000000-0000-0000-0000-000000000005', 'sofiawave', 'sofia@newzic.com', '$2a$10$dXJ3SW6G7P50lGmMQoeGbOwS3tY6bGHvUwEA3.LyH2rMd/1hSSfLO', 'Sofia Wave', 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&h=200&fit=crop&crop=face', 'https://images.unsplash.com/photo-1557683316-973673baf926?w=1200&h=400&fit=crop', 'R&B vocalist with a soulful voice and modern production.', 'Sofia blends classic R&B vocals with modern trap and electronic production. Her voice has been compared to a mix of SZA and Jorja Smith.', 24500, 67, 890000, false, 'London, UK', true, 'Looking for beatmakers to collaborate with', 12.5, 'https://open.spotify.com/artist/sofia', 'https://instagram.com/sofiawave', 'https://tiktok.com/@sofiawave', NULL),

('a1000000-0000-0000-0000-000000000006', 'beatsbyjake', 'jake@newzic.com', '$2a$10$dXJ3SW6G7P50lGmMQoeGbOwS3tY6bGHvUwEA3.LyH2rMd/1hSSfLO', 'Beats by Jake', 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200&h=200&fit=crop&crop=face', 'https://images.unsplash.com/photo-1557683316-973673baf926?w=1200&h=400&fit=crop', 'Hip-hop & trap beatmaker. Type beats and custom instrumentals.', 'Jake has been making beats since high school. Specializing in trap and hip-hop production, he sells beats online and collaborates with rappers worldwide.', 18900, 200, 560000, false, 'Atlanta, USA', true, 'Available for custom beat production', 8.3, NULL, 'https://instagram.com/beatsbyjake', 'https://tiktok.com/@beatsbyjake', NULL),

('a1000000-0000-0000-0000-000000000007', 'ameliasings', 'amelia@newzic.com', '$2a$10$dXJ3SW6G7P50lGmMQoeGbOwS3tY6bGHvUwEA3.LyH2rMd/1hSSfLO', 'Amelia Chen', 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=200&h=200&fit=crop&crop=face', 'https://images.unsplash.com/photo-1557683316-973673baf926?w=1200&h=400&fit=crop', 'Classical-trained pianist turned pop songwriter.', 'Amelia studied classical piano at the Royal Academy of Music before pivoting to pop songwriting. Her music blends classical arrangements with modern pop sensibilities.', 15600, 45, 420000, true, 'Tokyo, Japan', false, NULL, 5.1, 'https://open.spotify.com/artist/amelia', 'https://instagram.com/ameliasings', NULL, NULL),

('a1000000-0000-0000-0000-000000000008', 'neonpulse', 'neon@newzic.com', '$2a$10$dXJ3SW6G7P50lGmMQoeGbOwS3tY6bGHvUwEA3.LyH2rMd/1hSSfLO', 'Neon Pulse', 'https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=200&h=200&fit=crop&crop=face', 'https://images.unsplash.com/photo-1557683316-973673baf926?w=1200&h=400&fit=crop', 'Synthwave producer crafting retro-futuristic soundscapes.', 'Neon Pulse creates synthwave and retrowave music inspired by 80s aesthetics and modern electronic production. Each track is a journey through neon-lit cityscapes.', 22300, 30, 730000, false, 'Los Angeles, USA', true, 'Looking for vocalists for synthwave tracks', 9.7, 'https://open.spotify.com/artist/neonpulse', 'https://instagram.com/neonpulse', NULL, 'https://soundcloud.com/neonpulse');

-- Roles
INSERT INTO user_roles (user_id, role) VALUES
('a1000000-0000-0000-0000-000000000001', 'singer'),
('a1000000-0000-0000-0000-000000000002', 'producer'),
('a1000000-0000-0000-0000-000000000003', 'producer'),
('a1000000-0000-0000-0000-000000000004', 'band'),
('a1000000-0000-0000-0000-000000000005', 'singer'),
('a1000000-0000-0000-0000-000000000006', 'beatmaker'),
('a1000000-0000-0000-0000-000000000007', 'musician'),
('a1000000-0000-0000-0000-000000000008', 'producer');

-- Genres
INSERT INTO user_genres (user_id, genre) VALUES
('a1000000-0000-0000-0000-000000000001', 'Pop'), ('a1000000-0000-0000-0000-000000000001', 'Indie'),
('a1000000-0000-0000-0000-000000000002', 'Lo-fi'), ('a1000000-0000-0000-0000-000000000002', 'Chill'),
('a1000000-0000-0000-0000-000000000003', 'Electronic'), ('a1000000-0000-0000-0000-000000000003', 'EDM'), ('a1000000-0000-0000-0000-000000000003', 'Bass'),
('a1000000-0000-0000-0000-000000000004', 'Indie Rock'), ('a1000000-0000-0000-0000-000000000004', 'Alternative'),
('a1000000-0000-0000-0000-000000000005', 'R&B'), ('a1000000-0000-0000-0000-000000000005', 'Soul'),
('a1000000-0000-0000-0000-000000000006', 'Hip-Hop'), ('a1000000-0000-0000-0000-000000000006', 'Trap'),
('a1000000-0000-0000-0000-000000000007', 'Pop'), ('a1000000-0000-0000-0000-000000000007', 'Classical'),
('a1000000-0000-0000-0000-000000000008', 'Synthwave'), ('a1000000-0000-0000-0000-000000000008', 'Retrowave');

-- Tags
INSERT INTO user_tags (user_id, tag) VALUES
('a1000000-0000-0000-0000-000000000001', 'songwriter'), ('a1000000-0000-0000-0000-000000000001', 'vocalist'),
('a1000000-0000-0000-0000-000000000002', 'lo-fi'), ('a1000000-0000-0000-0000-000000000002', 'bedroom-producer'),
('a1000000-0000-0000-0000-000000000003', 'DJ'), ('a1000000-0000-0000-0000-000000000003', 'bass-music'),
('a1000000-0000-0000-0000-000000000004', 'live-band'), ('a1000000-0000-0000-0000-000000000004', 'garage-rock'),
('a1000000-0000-0000-0000-000000000005', 'vocalist'), ('a1000000-0000-0000-0000-000000000005', 'modern-rnb'),
('a1000000-0000-0000-0000-000000000006', 'type-beats'), ('a1000000-0000-0000-0000-000000000006', 'custom-beats'),
('a1000000-0000-0000-0000-000000000007', 'pianist'), ('a1000000-0000-0000-0000-000000000007', 'songwriter'),
('a1000000-0000-0000-0000-000000000008', '80s'), ('a1000000-0000-0000-0000-000000000008', 'retro');

-- ── ALBUMS ──
INSERT INTO albums (id, title, artist_id, cover, type, release_date, genre, description) VALUES
('b1000000-0000-0000-0000-000000000001', 'Notti di Vetro', 'a1000000-0000-0000-0000-000000000001', 'https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?w=300&h=300&fit=crop', 'EP', '2025-03-15', 'Pop', 'Marco''s debut EP blending pop melodies with indie storytelling.'),
('b1000000-0000-0000-0000-000000000002', 'Midnight Sessions', 'a1000000-0000-0000-0000-000000000002', 'https://images.unsplash.com/photo-1557672172-298e090bd0f1?w=300&h=300&fit=crop', 'ALBUM', '2025-01-20', 'Lo-fi', 'A collection of late-night lo-fi beats for studying and relaxing.'),
('b1000000-0000-0000-0000-000000000003', 'Bass Cathedral', 'a1000000-0000-0000-0000-000000000003', 'https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=300&h=300&fit=crop', 'ALBUM', '2025-05-01', 'Electronic', 'Heavy bass meets atmospheric production.'),
('b1000000-0000-0000-0000-000000000004', 'Garage Anthems', 'a1000000-0000-0000-0000-000000000004', 'https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?w=300&h=300&fit=crop', 'ALBUM', '2025-02-10', 'Indie Rock', 'Raw recordings from the Manchester garage where it all started.');

-- ── SONGS ──
INSERT INTO songs (id, title, artist_id, album_id, cover, duration, genre, release_date, plays, likes, audio_url, is_explicit, reactions_fire, reactions_gem, reactions_onpoint, reactions_star) VALUES
('c1000000-0000-0000-0000-000000000001', 'Vetro Rotto', 'a1000000-0000-0000-0000-000000000001', 'b1000000-0000-0000-0000-000000000001', 'https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?w=300&h=300&fit=crop', 234, 'Pop', '2025-03-15', 847000, 23400, NULL, false, 5200, 1800, 3100, 2400),
('c1000000-0000-0000-0000-000000000002', 'Milano Neon', 'a1000000-0000-0000-0000-000000000001', 'b1000000-0000-0000-0000-000000000001', 'https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?w=300&h=300&fit=crop', 198, 'Indie Pop', '2025-03-15', 512000, 15200, NULL, false, 3800, 1200, 2200, 1900),
('c1000000-0000-0000-0000-000000000003', 'Rainy Afternoons', 'a1000000-0000-0000-0000-000000000002', 'b1000000-0000-0000-0000-000000000002', 'https://images.unsplash.com/photo-1557672172-298e090bd0f1?w=300&h=300&fit=crop', 180, 'Lo-fi', '2025-01-20', 623000, 18700, NULL, false, 4100, 2500, 1900, 3200),
('c1000000-0000-0000-0000-000000000004', 'Cloudy Skies', 'a1000000-0000-0000-0000-000000000002', 'b1000000-0000-0000-0000-000000000002', 'https://images.unsplash.com/photo-1557672172-298e090bd0f1?w=300&h=300&fit=crop', 165, 'Lo-fi', '2025-01-20', 398000, 11200, NULL, false, 2900, 1700, 1400, 2100),
('c1000000-0000-0000-0000-000000000005', 'Bass Drop Alpha', 'a1000000-0000-0000-0000-000000000003', 'b1000000-0000-0000-0000-000000000003', 'https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=300&h=300&fit=crop', 276, 'Electronic', '2025-05-01', 456000, 12800, NULL, false, 6100, 900, 4200, 1500),
('c1000000-0000-0000-0000-000000000006', 'Neon Cathedral', 'a1000000-0000-0000-0000-000000000003', 'b1000000-0000-0000-0000-000000000003', 'https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=300&h=300&fit=crop', 312, 'Electronic', '2025-05-01', 289000, 8900, NULL, false, 3500, 1100, 2800, 1200),
('c1000000-0000-0000-0000-000000000007', 'Concrete Dreams', 'a1000000-0000-0000-0000-000000000004', 'b1000000-0000-0000-0000-000000000004', 'https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?w=300&h=300&fit=crop', 245, 'Indie Rock', '2025-02-10', 734000, 21500, NULL, false, 4800, 2100, 3600, 2800),
('c1000000-0000-0000-0000-000000000008', 'Last Train Home', 'a1000000-0000-0000-0000-000000000004', 'b1000000-0000-0000-0000-000000000004', 'https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?w=300&h=300&fit=crop', 267, 'Indie Rock', '2025-02-10', 521000, 16800, NULL, false, 3900, 1800, 2900, 2500),
('c1000000-0000-0000-0000-000000000009', 'Velvet Dreams', 'a1000000-0000-0000-0000-000000000005', NULL, 'https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=300&h=300&fit=crop', 225, 'R&B', '2025-06-01', 312000, 9800, NULL, false, 3200, 2800, 1500, 4100),
('c1000000-0000-0000-0000-000000000010', 'Trap Lord 808', 'a1000000-0000-0000-0000-000000000006', NULL, 'https://images.unsplash.com/photo-1598488035139-bdbb2231ce04?w=300&h=300&fit=crop', 190, 'Trap', '2025-05-20', 178000, 6200, NULL, true, 4500, 600, 3800, 800),
('c1000000-0000-0000-0000-000000000011', 'Moonlight Sonata Reimagined', 'a1000000-0000-0000-0000-000000000007', NULL, 'https://images.unsplash.com/photo-1511735111819-9a3f7709049c?w=300&h=300&fit=crop', 298, 'Classical Pop', '2025-04-10', 245000, 8900, NULL, false, 1200, 5600, 900, 6200),
('c1000000-0000-0000-0000-000000000012', 'Retrowave Drive', 'a1000000-0000-0000-0000-000000000008', NULL, 'https://images.unsplash.com/photo-1504704911898-68304a7d2807?w=300&h=300&fit=crop', 256, 'Synthwave', '2025-04-25', 198000, 7100, NULL, false, 3100, 1900, 2600, 2200);

-- Song tags
INSERT INTO song_tags (song_id, tag) VALUES
('c1000000-0000-0000-0000-000000000001', 'pop'), ('c1000000-0000-0000-0000-000000000001', 'italian'),
('c1000000-0000-0000-0000-000000000002', 'indie'), ('c1000000-0000-0000-0000-000000000002', 'milan'),
('c1000000-0000-0000-0000-000000000003', 'lo-fi'), ('c1000000-0000-0000-0000-000000000003', 'chill'),
('c1000000-0000-0000-0000-000000000005', 'bass'), ('c1000000-0000-0000-0000-000000000005', 'edm'),
('c1000000-0000-0000-0000-000000000007', 'indie-rock'), ('c1000000-0000-0000-0000-000000000007', 'live'),
('c1000000-0000-0000-0000-000000000009', 'rnb'), ('c1000000-0000-0000-0000-000000000009', 'soul'),
('c1000000-0000-0000-0000-000000000010', 'trap'), ('c1000000-0000-0000-0000-000000000010', '808'),
('c1000000-0000-0000-0000-000000000012', 'synthwave'), ('c1000000-0000-0000-0000-000000000012', 'retro');

-- ── FEED POSTS ──
INSERT INTO feed_posts (id, type, author_id, content, image, song_id, likes, comments, reactions_fire, reactions_gem, reactions_onpoint, reactions_star) VALUES
('d1000000-0000-0000-0000-000000000001', 'new_release', 'a1000000-0000-0000-0000-000000000001', 'Just dropped my new EP "Notti di Vetro"! 🎵 Three tracks that tell the story of late nights in Milan. Listen now and let me know what you think!', NULL, 'c1000000-0000-0000-0000-000000000001', 342, 28, 120, 45, 89, 67),
('d1000000-0000-0000-0000-000000000002', 'behind_the_scenes', 'a1000000-0000-0000-0000-000000000002', 'Late night studio session vibes 🌙 Working on some new beats. This one has a special feel to it... stay tuned!', 'https://images.unsplash.com/photo-1598488035139-bdbb2231ce04?w=600&h=400&fit=crop', NULL, 215, 15, 78, 32, 56, 41),
('d1000000-0000-0000-0000-000000000003', 'milestone', 'a1000000-0000-0000-0000-000000000003', 'Just hit 30K followers! 🎉 Thank you all for the incredible support. DRVGN fam is the best fam. New music coming soon to celebrate!', NULL, NULL, 567, 42, 200, 89, 134, 112),
('d1000000-0000-0000-0000-000000000004', 'new_release', 'a1000000-0000-0000-0000-000000000004', 'Concrete Dreams is OUT NOW 🏗️ Raw, unfiltered, straight from the garage. Turn it up and let it rip!', NULL, 'c1000000-0000-0000-0000-000000000007', 423, 35, 156, 78, 112, 94),
('d1000000-0000-0000-0000-000000000005', 'collab_request', 'a1000000-0000-0000-0000-000000000005', 'Looking for a producer who can blend trap beats with soulful R&B vibes. If that sounds like you, hit me up! 🎤✨', NULL, NULL, 189, 22, 45, 67, 34, 52),
('d1000000-0000-0000-0000-000000000006', 'update', 'a1000000-0000-0000-0000-000000000006', 'New beat pack dropping this weekend! 5 exclusive trap instrumentals, all free to use with credit. Keep an eye on my profile 🔥', NULL, NULL, 156, 18, 89, 23, 67, 31),
('d1000000-0000-0000-0000-000000000007', 'snippet', 'a1000000-0000-0000-0000-000000000008', 'Preview of my upcoming track "Chrome Sunset" 🌅 Full synthwave vibes, inspired by midnight drives through LA.', 'https://images.unsplash.com/photo-1504704911898-68304a7d2807?w=600&h=400&fit=crop', 'c1000000-0000-0000-0000-000000000012', 234, 19, 98, 56, 78, 45);

-- ── COLLABORATIONS ──
INSERT INTO collaborations (id, title, description, author_id, category, status, responses) VALUES
('e1000000-0000-0000-0000-000000000001', 'Need a vocalist for indie pop track', 'I have a full instrumental ready — looking for a vocalist who can deliver emotional, breathy vocals over indie pop production. Think Clairo meets Phoebe Bridgers.', 'a1000000-0000-0000-0000-000000000002', 'VOCALIST', 'OPEN', 5),
('e1000000-0000-0000-0000-000000000002', 'Trap beat collab — 808 heavy', 'Looking for another beatmaker to co-produce a series of trap beats. Must be comfortable with heavy 808s and hi-hat rolls.', 'a1000000-0000-0000-0000-000000000006', 'BEATMAKER', 'OPEN', 3),
('e1000000-0000-0000-0000-000000000003', 'Mixing engineer for EP', 'Our EP "Garage Anthems" needs professional mixing. 8 tracks, raw recordings. Looking for someone who understands indie rock sound.', 'a1000000-0000-0000-0000-000000000004', 'MIXING', 'OPEN', 7),
('e1000000-0000-0000-0000-000000000004', 'Songwriter for R&B album', 'Working on my debut album and need a co-writer for 3-4 tracks. Themes: love, growth, late nights. R&B/Soul vibe.', 'a1000000-0000-0000-0000-000000000005', 'SONGWRITER', 'OPEN', 4);

INSERT INTO collab_genres (collab_id, genre) VALUES
('e1000000-0000-0000-0000-000000000001', 'Indie Pop'), ('e1000000-0000-0000-0000-000000000001', 'Dream Pop'),
('e1000000-0000-0000-0000-000000000002', 'Trap'), ('e1000000-0000-0000-0000-000000000002', 'Hip-Hop'),
('e1000000-0000-0000-0000-000000000003', 'Indie Rock'), ('e1000000-0000-0000-0000-000000000003', 'Alternative'),
('e1000000-0000-0000-0000-000000000004', 'R&B'), ('e1000000-0000-0000-0000-000000000004', 'Soul');

INSERT INTO collab_tags (collab_id, tag) VALUES
('e1000000-0000-0000-0000-000000000001', 'vocals'), ('e1000000-0000-0000-0000-000000000001', 'indie'),
('e1000000-0000-0000-0000-000000000002', '808'), ('e1000000-0000-0000-0000-000000000002', 'beats'),
('e1000000-0000-0000-0000-000000000003', 'mixing'), ('e1000000-0000-0000-0000-000000000003', 'rock'),
('e1000000-0000-0000-0000-000000000004', 'songwriting'), ('e1000000-0000-0000-0000-000000000004', 'rnb');

-- ── SPOTLIGHT ──
INSERT INTO spotlights (id, artist_id, quote, featured_song_id, editor_note, week_label, active) VALUES
('f1000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000001', 'Music is how I process the world. Every song is a piece of my soul, wrapped in melody.', 'c1000000-0000-0000-0000-000000000001', 'Marco Rossi is one of the most exciting new voices in Italian indie pop. His EP "Notti di Vetro" showcases a songwriter who is both deeply personal and universally relatable.', 'Week of June 16, 2025', true);

-- ── FOLLOWS (some relationships) ──
INSERT INTO follows (id, follower_id, following_id) VALUES
('11000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000002', 'a1000000-0000-0000-0000-000000000001'),
('11000000-0000-0000-0000-000000000002', 'a1000000-0000-0000-0000-000000000005', 'a1000000-0000-0000-0000-000000000001'),
('11000000-0000-0000-0000-000000000003', 'a1000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000004'),
('11000000-0000-0000-0000-000000000004', 'a1000000-0000-0000-0000-000000000003', 'a1000000-0000-0000-0000-000000000006');

-- ── NOTIFICATIONS ──
INSERT INTO notifications (id, type, message, recipient_id, from_user_id, link, is_read) VALUES
('22000000-0000-0000-0000-000000000001', 'FOLLOW', 'Luna Vega started following you!', 'a1000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000002', '/artist/a1000000-0000-0000-0000-000000000002', false),
('22000000-0000-0000-0000-000000000002', 'LIKE', 'DRVGN liked your track "Vetro Rotto"', 'a1000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000003', '/artist/a1000000-0000-0000-0000-000000000003', false),
('22000000-0000-0000-0000-000000000003', 'MILESTONE', 'Congratulations! "Vetro Rotto" reached 800K plays! 🎉', 'a1000000-0000-0000-0000-000000000001', NULL, NULL, true),
('22000000-0000-0000-0000-000000000004', 'FOLLOW', 'Sofia Wave started following you!', 'a1000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000005', '/artist/a1000000-0000-0000-0000-000000000005', false);
