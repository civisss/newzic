package com.newzic.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "workspace_activities")
class WorkspaceActivityEntity(

    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    val workspace: WorkspaceEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserEntity,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: ActivityType,

    @Column(columnDefinition = "TEXT", nullable = false)
    val message: String,

    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class ActivityType {
    VERSION_UPLOADED,
    COMMENT_ADDED,
    COMMENT_RESOLVED,
    TASK_CREATED,
    TASK_COMPLETED,
    TASK_ASSIGNED,
    MEMBER_JOINED,
    STATUS_CHANGED,
    REFERENCE_ADDED
}
