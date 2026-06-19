package com.newzic.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(
    name = "reactions",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "song_id", "type"])]
)
class ReactionEntity(

    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", nullable = false)
    var song: SongEntity,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: ReactionType,

    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class ReactionType {
    FIRE, GEM, ONPOINT, STAR
}
