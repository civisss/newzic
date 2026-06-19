package com.newzic.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "feed_posts")
class FeedPostEntity(

    @Id
    val id: UUID = UUID.randomUUID(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: FeedPostType,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    var author: UserEntity,

    @Column(columnDefinition = "TEXT", nullable = false)
    var content: String,

    var image: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id")
    var song: SongEntity? = null,

    var likes: Long = 0,

    var comments: Long = 0,

    var reactionsFire: Long = 0,
    var reactionsGem: Long = 0,
    var reactionsOnpoint: Long = 0,
    var reactionsStar: Long = 0,

    val createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class FeedPostType {
    NEW_RELEASE, SNIPPET, BEHIND_THE_SCENES, MILESTONE, COLLAB_REQUEST, UPDATE
}
