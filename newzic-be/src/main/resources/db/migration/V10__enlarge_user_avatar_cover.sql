-- Allow avatar and cover to store base64 data URIs
ALTER TABLE users ALTER COLUMN avatar TYPE TEXT;
ALTER TABLE users ALTER COLUMN cover TYPE TEXT;
