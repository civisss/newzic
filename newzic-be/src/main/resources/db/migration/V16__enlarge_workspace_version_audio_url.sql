-- V16: Enlarge workspace_versions.audio_url to TEXT for base64 data URLs
ALTER TABLE workspace_versions ALTER COLUMN audio_url TYPE TEXT;
