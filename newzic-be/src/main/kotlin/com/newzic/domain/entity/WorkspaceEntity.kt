package com.newzic.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "workspaces")
class WorkspaceEntity(

    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    var title: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: WorkspaceStatus = WorkspaceStatus.DRAFT,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    val owner: UserEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collaboration_id")
    var collaboration: CollaborationEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "published_song_id")
    var publishedSong: SongEntity? = null,

    val createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class WorkspaceStatus {
    DRAFT, IN_PROGRESS, REVIEW, DONE
}
