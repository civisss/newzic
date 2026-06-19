-- Allow audio_url to store base64 data URIs
ALTER TABLE songs ALTER COLUMN audio_url TYPE TEXT;
