package com.newzic.domain.entity

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "albums")
class AlbumEntity(

    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    var title: String,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "artist_id", nullable = false)
    var artist: UserEntity,

    var cover: String? = null,

    @Enumerated(EnumType.STRING)
    var type: AlbumType = AlbumType.ALBUM,

    var releaseDate: LocalDate = LocalDate.now(),

    var genre: String? = null,

    var totalPlays: Long = 0,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    val createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class AlbumType {
    ALBUM, EP, SINGLE
}
