package com.newzic.domain.repository

import com.newzic.domain.entity.JournalCommentEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface JournalCommentRepository : JpaRepository<JournalCommentEntity, UUID> {

    fun findByPostIdOrderByCreatedAtAsc(postId: UUID, pageable: Pageable): Page<JournalCommentEntity>

    fun countByPostId(postId: UUID): Int
}
