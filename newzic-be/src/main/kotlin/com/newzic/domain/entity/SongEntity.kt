package com.newzic.domain.entity

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "songs")
class SongEntity(

    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    var title: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id", nullable = false)
    var artist: UserEntity,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "album_id")
    var album: AlbumEntity? = null,

    @Column(columnDefinition = "TEXT")
    var cover: String? = null,

    var duration: Int = 0, // seconds

    var genre: String? = null,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "song_tags", joinColumns = [JoinColumn(name = "song_id")])
    @Column(name = "tag")
    var tags: MutableSet<String> = mutableSetOf(),

    var releaseDate: LocalDate = LocalDate.now(),

    var plays: Long = 0,

    var likes: Long = 0,

    @Column(columnDefinition = "TEXT")
    var audioUrl: String? = null,

    var isExplicit: Boolean = false,

    // Reactions (denormalized counters for performance)
    var reactionsFire: Long = 0,
    var reactionsGem: Long = 0,
    var reactionsOnpoint: Long = 0,
    var reactionsStar: Long = 0,

    val createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime = LocalDateTime.now()
)
