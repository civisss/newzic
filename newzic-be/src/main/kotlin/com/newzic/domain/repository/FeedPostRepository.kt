package com.newzic.domain.repository

import com.newzic.domain.entity.FeedPostEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface FeedPostRepository : JpaRepository<FeedPostEntity, UUID> {

    @EntityGraph(attributePaths = ["author", "author.roles", "song", "song.artist"])
    fun findByAuthorId(authorId: UUID, pageable: Pageable): Page<FeedPostEntity>

    @EntityGraph(attributePaths = ["author", "author.roles", "song", "song.artist"])
    fun findAllByOrderByCreatedAtDesc(pageable: Pageable): Page<FeedPostEntity>
}
