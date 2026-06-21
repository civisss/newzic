package com.newzic.domain.repository

import com.newzic.domain.entity.SongLikeEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface SongLikeRepository : JpaRepository<SongLikeEntity, UUID> {

    fun findByUserIdAndSongId(userId: UUID, songId: UUID): SongLikeEntity?

    fun existsByUserIdAndSongId(userId: UUID, songId: UUID): Boolean

    @Query("SELECT sl FROM SongLikeEntity sl JOIN FETCH sl.song s JOIN FETCH s.artist WHERE sl.user.id = :userId ORDER BY sl.createdAt DESC")
    fun findByUserIdOrderByCreatedAtDesc(userId: UUID): List<SongLikeEntity>

    fun countByUserId(userId: UUID): Long

    @Query("""
        SELECT sl.song.genre, COUNT(sl) FROM SongLikeEntity sl 
        WHERE sl.user.id = :userId AND sl.song.genre IS NOT NULL
        GROUP BY sl.song.genre ORDER BY COUNT(sl) DESC
    """)
    fun findGenreAffinitiesByUserId(userId: UUID): List<Array<Any>>
}
