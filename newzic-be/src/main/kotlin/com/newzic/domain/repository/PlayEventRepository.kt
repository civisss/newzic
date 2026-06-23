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

    @Query("""
        SELECT p.song.genre, COUNT(p) FROM PlayEventEntity p 
        WHERE p.song.artist.id = :artistId AND p.song.genre IS NOT NULL
        GROUP BY p.song.genre ORDER BY COUNT(p) DESC
    """)
    fun findTopGenresByArtist(artistId: UUID): List<Array<Any>>

    @Query("""
        SELECT p.song.genre, COUNT(p) FROM PlayEventEntity p 
        WHERE p.user.id = :userId AND p.song.genre IS NOT NULL
        GROUP BY p.song.genre ORDER BY COUNT(p) DESC
    """)
    fun findGenreAffinitiesByUserId(userId: UUID): List<Array<Any>>

    @Query("""
        SELECT p.song.artist.country, COUNT(p) FROM PlayEventEntity p 
        WHERE p.user.id = :userId AND p.song.artist.country IS NOT NULL
        GROUP BY p.song.artist.country ORDER BY COUNT(p) DESC
    """)
    fun findCountryAffinitiesByUserId(userId: UUID): List<Array<Any>>
}
