package com.newzic.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "journal_posts")
class JournalPostEntity(

    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    var author: UserEntity,

    @Column(columnDefinition = "TEXT", nullable = false)
    var content: String,

    @Column(columnDefinition = "TEXT")
    var imageUrl: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var category: JournalCategory = JournalCategory.UPDATE,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "journal_post_hashtags", joinColumns = [JoinColumn(name = "post_id")])
    @Column(name = "hashtag")
    var hashtags: MutableSet<String> = mutableSetOf(),

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "journal_post_tagged_users", joinColumns = [JoinColumn(name = "post_id")])
    @Column(name = "user_id")
    var taggedUserIds: MutableSet<UUID> = mutableSetOf(),

    var reactionCount: Int = 0,

    var commentCount: Int = 0,

    val createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class JournalCategory {
    UPDATE, COLLABORATION, LOOKING_FOR_COLLAB, ANNOUNCEMENT
}
