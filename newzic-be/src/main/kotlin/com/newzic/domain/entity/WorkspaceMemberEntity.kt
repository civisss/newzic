package com.newzic.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(
    name = "workspace_members",
    uniqueConstraints = [UniqueConstraint(columnNames = ["workspace_id", "user_id"])]
)
class WorkspaceMemberEntity(

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
    var role: WorkspaceMemberRole = WorkspaceMemberRole.COLLABORATOR,

    val joinedAt: LocalDateTime = LocalDateTime.now()
)

enum class WorkspaceMemberRole {
    OWNER, COLLABORATOR
}
