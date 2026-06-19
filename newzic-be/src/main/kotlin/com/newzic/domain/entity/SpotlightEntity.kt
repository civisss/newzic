package com.newzic.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "spotlights")
class SpotlightEntity(

    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id", nullable = false)
    var artist: UserEntity,

    @Column(columnDefinition = "TEXT", nullable = false)
    var quote: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "featured_song_id")
    var featuredSong: SongEntity? = null,

    @Column(columnDefinition = "TEXT")
    var editorNote: String? = null,

    @Column(nullable = false)
    var weekLabel: String,

    var active: Boolean = true,

    val createdAt: LocalDateTime = LocalDateTime.now()
)
