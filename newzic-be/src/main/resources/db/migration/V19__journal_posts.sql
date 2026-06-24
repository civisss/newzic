-- Journal Posts
CREATE TABLE journal_posts (
    id UUID PRIMARY KEY,
    author_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    image_url TEXT,
    category VARCHAR(50) NOT NULL DEFAULT 'UPDATE',
    reaction_count INT NOT NULL DEFAULT 0,
    comment_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_journal_posts_author ON journal_posts(author_id);
CREATE INDEX idx_journal_posts_created ON journal_posts(created_at DESC);

-- Journal Post Hashtags
CREATE TABLE journal_post_hashtags (
    post_id UUID NOT NULL REFERENCES journal_posts(id) ON DELETE CASCADE,
    hashtag VARCHAR(100) NOT NULL,
    PRIMARY KEY (post_id, hashtag)
);

CREATE INDEX idx_journal_hashtags ON journal_post_hashtags(hashtag);

-- Journal Post Tagged Users
CREATE TABLE journal_post_tagged_users (
    post_id UUID NOT NULL REFERENCES journal_posts(id) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    PRIMARY KEY (post_id, user_id)
);

-- Journal Reactions
CREATE TABLE journal_reactions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    post_id UUID NOT NULL REFERENCES journal_posts(id) ON DELETE CASCADE,
    type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, post_id, type)
);

CREATE INDEX idx_journal_reactions_post ON journal_reactions(post_id);

-- Journal Comments
CREATE TABLE journal_comments (
    id UUID PRIMARY KEY,
    post_id UUID NOT NULL REFERENCES journal_posts(id) ON DELETE CASCADE,
    author_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_journal_comments_post ON journal_comments(post_id);
