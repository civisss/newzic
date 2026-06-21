package com.newzic.domain.repository

import com.newzic.domain.entity.SongEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface SongRepository : JpaRepository<SongEntity, UUID> {

    @EntityGraph(attributePaths = ["artist", "album", "tags"])
    fun findByArtistId(artistId: UUID, pageable: Pageable): Page<SongEntity>

    fun findByArtistId(artistId: UUID): List<SongEntity>

    @EntityGraph(attributePaths = ["artist", "album", "tags"])
    fun findByAlbumId(albumId: UUID): List<SongEntity>

    @EntityGraph(attributePaths = ["artist", "album", "tags"])
    @Query("SELECT s FROM SongEntity s ORDER BY s.plays DESC")
    fun findTrending(pageable: Pageable): Page<SongEntity>

    @EntityGraph(attributePaths = ["artist", "album", "tags"])
    @Query("SELECT s FROM SongEntity s ORDER BY s.releaseDate DESC")
    fun findNewReleases(pageable: Pageable): Page<SongEntity>

    @EntityGraph(attributePaths = ["artist", "album", "tags"])
    @Query("""
        SELECT s FROM SongEntity s 
        WHERE LOWER(s.title) LIKE LOWER(CONCAT('%', :query, '%'))
        OR LOWER(s.genre) LIKE LOWER(CONCAT('%', :query, '%'))
    """)
    fun search(query: String, pageable: Pageable): Page<SongEntity>

    @EntityGraph(attributePaths = ["artist", "album", "tags"])
    @Query("SELECT s FROM SongEntity s WHERE s.genre = :genre ORDER BY s.plays DESC")
    fun findByGenre(genre: String, pageable: Pageable): Page<SongEntity>

    fun countByArtistId(artistId: UUID): Long

    @EntityGraph(attributePaths = ["artist", "album", "tags"])
    @Query("""
        SELECT s FROM SongEntity s 
        WHERE s.artist.id <> :excludeUserId
        AND (s.genre IN :genres OR s.artist.country = :country)
        ORDER BY s.plays DESC
    """)
    fun findRecommendationCandidates(
        @Param("excludeUserId") excludeUserId: UUID,
        @Param("genres") genres: Set<String>,
        @Param("country") country: String,
        pageable: Pageable
    ): Page<SongEntity>

    @EntityGraph(attributePaths = ["artist", "album", "tags"])
    @Query("""
        SELECT s FROM SongEntity s 
        WHERE s.artist.id <> :excludeUserId
        AND s.genre IN :genres
        ORDER BY s.plays DESC
    """)
    fun findByGenresExcludingUser(
        @Param("excludeUserId") excludeUserId: UUID,
        @Param("genres") genres: Set<String>,
        pageable: Pageable
    ): Page<SongEntity>

    @EntityGraph(attributePaths = ["artist", "album", "tags"])
    @Query("""
        SELECT s FROM SongEntity s 
        WHERE s.artist.id <> :excludeUserId
        AND s.artist.country = :country
        ORDER BY s.plays DESC
    """)
    fun findByArtistCountryExcludingUser(
        @Param("excludeUserId") excludeUserId: UUID,
        @Param("country") country: String,
        pageable: Pageable
    ): Page<SongEntity>
}
