package com.newzic.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "workspace_comments")
class WorkspaceCommentEntity(

    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_id", nullable = false)
    val version: WorkspaceVersionEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    val author: UserEntity,

    @Column(columnDefinition = "TEXT", nullable = false)
    var content: String,

    @Column(name = "timestamp_seconds", nullable = false)
    val timestampSeconds: Double,

    @Column(name = "end_timestamp_seconds")
    val endTimestampSeconds: Double? = null,

    var resolved: Boolean = false,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by_version_id")
    var resolvedByVersion: WorkspaceVersionEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    var parent: WorkspaceCommentEntity? = null,

    val createdAt: LocalDateTime = LocalDateTime.now()
)
