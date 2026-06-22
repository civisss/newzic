package com.newzic.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "workspace_chat_messages")
class WorkspaceChatEntity(

    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    val workspace: WorkspaceEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    val sender: UserEntity,

    @Column(columnDefinition = "TEXT", nullable = false)
    val content: String,

    val createdAt: LocalDateTime = LocalDateTime.now()
)
