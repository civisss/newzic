-- =============================================
-- V15: Collab Workspace tables
-- =============================================

-- Main workspace table
CREATE TABLE workspaces (
    id                UUID PRIMARY KEY,
    title             VARCHAR(200) NOT NULL,
    description       TEXT,
    status            VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    owner_id          UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    collaboration_id  UUID         REFERENCES collaborations(id) ON DELETE SET NULL,
    published_song_id UUID         REFERENCES songs(id) ON DELETE SET NULL,
    created_at        TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Members of each workspace
CREATE TABLE workspace_members (
    id           UUID PRIMARY KEY,
    workspace_id UUID        NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    user_id      UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role         VARCHAR(20) NOT NULL DEFAULT 'COLLABORATOR',
    joined_at    TIMESTAMP   NOT NULL DEFAULT NOW(),
    UNIQUE (workspace_id, user_id)
);

-- Audio versions (each upload is a new version)
CREATE TABLE workspace_versions (
    id             UUID PRIMARY KEY,
    workspace_id   UUID         NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    version_number INT          NOT NULL,
    audio_url      VARCHAR(1000) NOT NULL,
    notes          TEXT,
    uploaded_by    UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    duration       INT          NOT NULL DEFAULT 0,
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Timestamped comments on audio versions
CREATE TABLE workspace_comments (
    id                UUID PRIMARY KEY,
    version_id        UUID             NOT NULL REFERENCES workspace_versions(id) ON DELETE CASCADE,
    author_id         UUID             NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content           TEXT             NOT NULL,
    timestamp_seconds DOUBLE PRECISION NOT NULL,
    parent_id         UUID             REFERENCES workspace_comments(id) ON DELETE CASCADE,
    created_at        TIMESTAMP        NOT NULL DEFAULT NOW()
);

-- Shared files (stems, samples, references, lyrics)
CREATE TABLE workspace_files (
    id          UUID PRIMARY KEY,
    workspace_id UUID         NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    name        VARCHAR(255) NOT NULL,
    url         VARCHAR(1000) NOT NULL,
    file_type   VARCHAR(20)  NOT NULL DEFAULT 'OTHER',
    size_bytes  BIGINT       NOT NULL DEFAULT 0,
    uploaded_by UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Project chat messages
CREATE TABLE workspace_chat_messages (
    id           UUID PRIMARY KEY,
    workspace_id UUID      NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    sender_id    UUID      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content      TEXT      NOT NULL,
    created_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =============================================
-- Indexes
-- =============================================
CREATE INDEX idx_workspace_owner        ON workspaces(owner_id);
CREATE INDEX idx_workspace_status       ON workspaces(status);
CREATE INDEX idx_workspace_collab       ON workspaces(collaboration_id);

CREATE INDEX idx_ws_member_workspace    ON workspace_members(workspace_id);
CREATE INDEX idx_ws_member_user         ON workspace_members(user_id);

CREATE INDEX idx_ws_version_workspace   ON workspace_versions(workspace_id);
CREATE INDEX idx_ws_version_number      ON workspace_versions(workspace_id, version_number DESC);

CREATE INDEX idx_ws_comment_version     ON workspace_comments(version_id);
CREATE INDEX idx_ws_comment_timestamp   ON workspace_comments(version_id, timestamp_seconds);
CREATE INDEX idx_ws_comment_parent      ON workspace_comments(parent_id);

CREATE INDEX idx_ws_file_workspace      ON workspace_files(workspace_id);

CREATE INDEX idx_ws_chat_workspace      ON workspace_chat_messages(workspace_id, created_at);
