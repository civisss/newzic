package com.newzic.domain.repository

import com.newzic.domain.entity.JournalReactionEntity
import com.newzic.domain.entity.JournalReactionType
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface JournalReactionRepository : JpaRepository<JournalReactionEntity, UUID> {

    fun findByPostIdAndUserId(postId: UUID, userId: UUID): List<JournalReactionEntity>

    fun findByPostId(postId: UUID): List<JournalReactionEntity>

    fun existsByPostIdAndUserIdAndType(postId: UUID, userId: UUID, type: JournalReactionType): Boolean

    fun deleteByPostIdAndUserIdAndType(postId: UUID, userId: UUID, type: JournalReactionType)

    fun countByPostId(postId: UUID): Int

    fun countByPostIdAndType(postId: UUID, type: JournalReactionType): Int
}
