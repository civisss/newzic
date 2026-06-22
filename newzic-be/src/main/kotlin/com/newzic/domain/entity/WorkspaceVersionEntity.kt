package com.newzic.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "workspace_versions")
class WorkspaceVersionEntity(

    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    val workspace: WorkspaceEntity,

    @Column(nullable = false)
    val versionNumber: Int,

    @Column(nullable = false, columnDefinition = "TEXT")
    var audioUrl: String,

    @Column(columnDefinition = "TEXT")
    var notes: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    val uploadedBy: UserEntity,

    var duration: Int = 0,

    val createdAt: LocalDateTime = LocalDateTime.now()
)
