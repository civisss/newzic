package com.newzic.domain.repository

import com.newzic.domain.entity.CollabStatus
import com.newzic.domain.entity.CollaborationEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CollaborationRepository : JpaRepository<CollaborationEntity, UUID> {

    @EntityGraph(attributePaths = ["author"])
    fun findByAuthorId(authorId: UUID, pageable: Pageable): Page<CollaborationEntity>

    @EntityGraph(attributePaths = ["author"])
    fun findByStatus(status: CollabStatus, pageable: Pageable): Page<CollaborationEntity>

    @EntityGraph(attributePaths = ["author"])
    fun findAllByOrderByCreatedAtDesc(pageable: Pageable): Page<CollaborationEntity>
}
