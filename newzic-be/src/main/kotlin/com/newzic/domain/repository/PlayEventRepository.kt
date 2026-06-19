package com.newzic.domain.repository

import com.newzic.domain.entity.PlayEventEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime
import java.util.UUID

interface PlayEventRepository : JpaRepository<PlayEventEntity, UUID> {

    fun countBySongId(songId: UUID): Long

    @Query("SELECT COUNT(p) FROM PlayEventEntity p WHERE p.song.artist.id = :artistId")
    fun countByArtistId(artistId: UUID): Long

    @Query("""
        SELECT p.city, COUNT(p) FROM PlayEventEntity p 
        WHERE p.song.artist.id = :artistId AND p.city IS NOT NULL
        GROUP BY p.city ORDER BY COUNT(p) DESC
    """)
    fun findTopCitiesByArtist(artistId: UUID): List<Array<Any>>

    @Query("SELECT COUNT(p) FROM PlayEventEntity p WHERE p.song.artist.id = :artistId AND p.createdAt >= :since")
    fun countByArtistIdSince(artistId: UUID, since: LocalDateTime): Long
}
