package com.newzic.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "collaborations")
class CollaborationEntity(

    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    var title: String,

    @Column(columnDefinition = "TEXT", nullable = false)
    var description: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    var author: UserEntity,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var category: CollabCategory,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "collab_genres", joinColumns = [JoinColumn(name = "collab_id")])
    @Column(name = "genre")
    var genres: MutableList<String> = mutableListOf(),

    @Enumerated(EnumType.STRING)
    var status: CollabStatus = CollabStatus.OPEN,

    var responses: Int = 0,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "collab_tags", joinColumns = [JoinColumn(name = "collab_id")])
    @Column(name = "tag")
    var tags: MutableList<String> = mutableListOf(),

    val createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class CollabCategory {
    VOCALIST, PRODUCER, BEATMAKER, GUITARIST, MIXING, MASTERING, SONGWRITER, OTHER
}

enum class CollabStatus {
    OPEN, IN_PROGRESS, CLOSED
}
