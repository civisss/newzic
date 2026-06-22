package com.newzic.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "workspace_files")
class WorkspaceFileEntity(

    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    val workspace: WorkspaceEntity,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    var url: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var fileType: WorkspaceFileType = WorkspaceFileType.OTHER,

    var sizeBytes: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    val uploadedBy: UserEntity,

    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class WorkspaceFileType {
    STEM, SAMPLE, REFERENCE, LYRICS, OTHER
}
