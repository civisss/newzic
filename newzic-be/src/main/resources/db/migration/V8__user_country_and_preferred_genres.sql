-- Add country to users for geographic recommendations
ALTER TABLE users ADD COLUMN country VARCHAR(100);

-- Preferred genres table (genres the user LIKES, not genres they produce)
CREATE TABLE user_preferred_genres (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    genre VARCHAR(100) NOT NULL,
    PRIMARY KEY (user_id, genre)
);

-- Set country for existing seed users (extracted from location)
UPDATE users SET country = 'IT' WHERE id = 'a1000000-0000-0000-0000-000000000001'; -- Marco Rossi, Milan
UPDATE users SET country = 'ES' WHERE id = 'a1000000-0000-0000-0000-000000000002'; -- Luna Vega, Barcelona
UPDATE users SET country = 'DE' WHERE id = 'a1000000-0000-0000-0000-000000000003'; -- DRVGN, Berlin
UPDATE users SET country = 'GB' WHERE id = 'a1000000-0000-0000-0000-000000000004'; -- Midnight Collective, Manchester
UPDATE users SET country = 'GB' WHERE id = 'a1000000-0000-0000-0000-000000000005'; -- Sofia Wave, London
UPDATE users SET country = 'US' WHERE id = 'a1000000-0000-0000-0000-000000000006'; -- Beats by Jake, Atlanta
UPDATE users SET country = 'JP' WHERE id = 'a1000000-0000-0000-0000-000000000007'; -- Amelia Chen, Tokyo
UPDATE users SET country = 'US' WHERE id = 'a1000000-0000-0000-0000-000000000008'; -- Neon Pulse, LA

-- Preferred genres for seed users (what they like to LISTEN to)
INSERT INTO user_preferred_genres (user_id, genre) VALUES
-- Marco Rossi likes: Indie Pop, Dream Pop, R&B
('a1000000-0000-0000-0000-000000000001', 'Indie Pop'),
('a1000000-0000-0000-0000-000000000001', 'Dream Pop'),
('a1000000-0000-0000-0000-000000000001', 'R&B'),
-- Luna Vega likes: Lo-Fi, Ambient, Neo-Soul
('a1000000-0000-0000-0000-000000000002', 'Lo-Fi'),
('a1000000-0000-0000-0000-000000000002', 'Ambient'),
('a1000000-0000-0000-0000-000000000002', 'Neo-Soul'),
-- DRVGN likes: Techno, House, Future Bass
('a1000000-0000-0000-0000-000000000003', 'Techno'),
('a1000000-0000-0000-0000-000000000003', 'House'),
('a1000000-0000-0000-0000-000000000003', 'Future Bass'),
-- Midnight Collective likes: Indie Rock, Post-Rock, Dream Pop
('a1000000-0000-0000-0000-000000000004', 'Indie Rock'),
('a1000000-0000-0000-0000-000000000004', 'Post-Rock'),
('a1000000-0000-0000-0000-000000000004', 'Dream Pop'),
-- Sofia Wave likes: R&B, Neo-Soul, Hip-Hop
('a1000000-0000-0000-0000-000000000005', 'R&B'),
('a1000000-0000-0000-0000-000000000005', 'Neo-Soul'),
('a1000000-0000-0000-0000-000000000005', 'Hip-Hop'),
-- Beats by Jake likes: Trap, Hip-Hop, Reggaeton
('a1000000-0000-0000-0000-000000000006', 'Trap'),
('a1000000-0000-0000-0000-000000000006', 'Hip-Hop'),
('a1000000-0000-0000-0000-000000000006', 'Reggaeton'),
-- Amelia Chen likes: Pop, Jazz, Ambient
('a1000000-0000-0000-0000-000000000007', 'Pop'),
('a1000000-0000-0000-0000-000000000007', 'Jazz'),
('a1000000-0000-0000-0000-000000000007', 'Ambient'),
-- Neon Pulse likes: Electronic, Synthwave, House
('a1000000-0000-0000-0000-000000000008', 'Electronic'),
('a1000000-0000-0000-0000-000000000008', 'House'),
('a1000000-0000-0000-0000-000000000008', 'Techno');

-- Demo admin user: Italian, likes Pop, Indie Rock, R&B
UPDATE users SET country = 'IT' WHERE id = 'a1000000-0000-0000-0000-000000000099';
INSERT INTO user_preferred_genres (user_id, genre) VALUES
('a1000000-0000-0000-0000-000000000099', 'Pop'),
('a1000000-0000-0000-0000-000000000099', 'Indie Rock'),
('a1000000-0000-0000-0000-000000000099', 'R&B');
