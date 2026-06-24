package com.newzic.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(
    name = "journal_reactions",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "post_id", "type"])]
)
class JournalReactionEntity(

    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    var post: JournalPostEntity,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: JournalReactionType,

    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class JournalReactionType {
    LIKE, FIRE, MUSIC, HYPE
}
