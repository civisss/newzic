package com.newzic.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "journal_comments")
class JournalCommentEntity(

    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    var post: JournalPostEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    var author: UserEntity,

    @Column(columnDefinition = "TEXT", nullable = false)
    var content: String,

    val createdAt: LocalDateTime = LocalDateTime.now()
)
