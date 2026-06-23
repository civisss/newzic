package com.newzic.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "workspace_references")
class WorkspaceReferenceEntity(

    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    val workspace: WorkspaceEntity,

    @Column(nullable = false)
    var title: String,

    var artist: String? = null,

    @Column(columnDefinition = "TEXT")
    var url: String? = null,

    @Column(columnDefinition = "TEXT")
    var notes: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var platform: ReferencePlatform = ReferencePlatform.OTHER,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "added_by_id", nullable = false)
    val addedBy: UserEntity,

    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class ReferencePlatform {
    SPOTIFY, YOUTUBE, SOUNDCLOUD, APPLE_MUSIC, OTHER
}
