package com.newzic.domain.repository

import com.newzic.domain.entity.JournalPostEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface JournalPostRepository : JpaRepository<JournalPostEntity, UUID> {

    fun findByAuthorIdOrderByCreatedAtDesc(authorId: UUID, pageable: Pageable): Page<JournalPostEntity>

    fun countByAuthorId(authorId: UUID): Long

    @Query("""
        SELECT p FROM JournalPostEntity p
        ORDER BY (p.reactionCount + p.commentCount * 2) DESC, p.createdAt DESC
    """)
    fun findFeedPosts(pageable: Pageable): Page<JournalPostEntity>

    @Query("""
        SELECT p FROM JournalPostEntity p
        WHERE LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%'))
        ORDER BY p.createdAt DESC
    """)
    fun searchByContent(query: String, pageable: Pageable): Page<JournalPostEntity>

    @Query("""
        SELECT p FROM JournalPostEntity p
        JOIN p.hashtags h
        WHERE LOWER(h) = LOWER(:hashtag)
        ORDER BY p.createdAt DESC
    """)
    fun findByHashtag(hashtag: String, pageable: Pageable): Page<JournalPostEntity>

    @Query("""
        SELECT p FROM JournalPostEntity p
        JOIN p.taggedUserIds t
        WHERE t = :userId
        ORDER BY p.createdAt DESC
    """)
    fun findByTaggedUserId(userId: UUID, pageable: Pageable): Page<JournalPostEntity>
}
