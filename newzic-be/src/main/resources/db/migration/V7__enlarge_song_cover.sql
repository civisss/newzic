-- Allow large base64 cover art in songs
ALTER TABLE songs ALTER COLUMN cover TYPE TEXT;
