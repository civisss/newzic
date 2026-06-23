-- =============================================
-- V17: Workspace collaboration features
--   - Enhanced comments (range, resolve)
--   - Version changelog
--   - Task board
--   - Activity feed
--   - Reference tracks
-- =============================================

-- ── Enhanced comments: range + resolve ──
ALTER TABLE workspace_comments
    ADD COLUMN end_timestamp_seconds DOUBLE PRECISION,
    ADD COLUMN resolved             BOOLEAN   NOT NULL DEFAULT FALSE,
    ADD COLUMN resolved_by_version_id UUID    REFERENCES workspace_versions(id) ON DELETE SET NULL;

-- ── Version changelog (element collection) ──
CREATE TABLE workspace_version_changelog (
    version_id UUID    NOT NULL REFERENCES workspace_versions(id) ON DELETE CASCADE,
    entry      TEXT    NOT NULL
);
CREATE INDEX idx_ws_version_changelog ON workspace_version_changelog(version_id);

-- ── Task board ──
CREATE TABLE workspace_tasks (
    id                  UUID PRIMARY KEY,
    workspace_id        UUID         NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    title               VARCHAR(500) NOT NULL,
    description         TEXT,
    status              VARCHAR(20)  NOT NULL DEFAULT 'TODO',
    assigned_to_id      UUID         REFERENCES users(id) ON DELETE SET NULL,
    created_by_id       UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    timestamp_seconds   DOUBLE PRECISION,
    resolved_by_version_id UUID      REFERENCES workspace_versions(id) ON DELETE SET NULL,
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP    NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_ws_task_workspace ON workspace_tasks(workspace_id);
CREATE INDEX idx_ws_task_status    ON workspace_tasks(workspace_id, status);

-- ── Activity feed ──
CREATE TABLE workspace_activities (
    id           UUID PRIMARY KEY,
    workspace_id UUID         NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    user_id      UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type         VARCHAR(30)  NOT NULL,
    message      TEXT         NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_ws_activity_workspace ON workspace_activities(workspace_id, created_at DESC);

-- ── Reference tracks ──
CREATE TABLE workspace_references (
    id           UUID PRIMARY KEY,
    workspace_id UUID         NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    title        VARCHAR(500) NOT NULL,
    artist       VARCHAR(255),
    url          VARCHAR(1000),
    notes        TEXT,
    platform     VARCHAR(20)  NOT NULL DEFAULT 'OTHER',
    added_by_id  UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_ws_reference_workspace ON workspace_references(workspace_id);
