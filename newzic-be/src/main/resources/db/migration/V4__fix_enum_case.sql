-- Fix enum case: roles must be UPPERCASE to match Kotlin enum ArtistRole
UPDATE user_roles SET role = UPPER(role);

-- Fix enum case: feed post types must be UPPERCASE to match Kotlin enum FeedPostType
UPDATE feed_posts SET type = UPPER(type);
