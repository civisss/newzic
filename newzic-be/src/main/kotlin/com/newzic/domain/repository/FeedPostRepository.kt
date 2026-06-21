package com.newzic.domain.repository

import com.newzic.domain.entity.FeedPostEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface FeedPostRepository : JpaRepository<FeedPostEntity, UUID> {

    @EntityGraph(attributePaths = ["author", "author.roles", "song", "song.artist"])
    fun findByAuthorId(authorId: UUID, pageable: Pageable): Page<FeedPostEntity>

    @EntityGraph(attributePaths = ["author", "author.roles", "song", "song.artist"])
    fun findAllByOrderByCreatedAtDesc(pageable: Pageable): Page<FeedPostEntity>

    @EntityGraph(attributePaths = ["author", "author.roles", "song", "song.artist"])
    @Query("""
        SELECT f FROM FeedPostEntity f 
        WHERE f.author.id IN :authorIds
        ORDER BY f.createdAt DESC
    """)
    fun findByAuthorIds(
        @Param("authorIds") authorIds: Set<UUID>,
        pageable: Pageable
    ): Page<FeedPostEntity>

    @EntityGraph(attributePaths = ["author", "author.roles", "song", "song.artist"])
    @Query("""
        SELECT f FROM FeedPostEntity f 
        WHERE f.author.id NOT IN :excludeIds
        ORDER BY f.likes DESC, f.createdAt DESC
    """)
    fun findDiscoverExcluding(
        @Param("excludeIds") excludeIds: Set<UUID>,
        pageable: Pageable
    ): Page<FeedPostEntity>
}
