package com.newzic.domain.repository

import com.newzic.domain.entity.ReactionEntity
import com.newzic.domain.entity.ReactionType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import java.util.UUID

interface ReactionRepository : JpaRepository<ReactionEntity, UUID> {

    fun findByUserIdAndSongId(userId: UUID, songId: UUID): List<ReactionEntity>

    fun findByUserIdAndSongIdAndType(userId: UUID, songId: UUID, type: ReactionType): ReactionEntity?

    fun countBySongIdAndType(songId: UUID, type: ReactionType): Long

    fun existsByUserIdAndSongIdAndType(userId: UUID, songId: UUID, type: ReactionType): Boolean

    @Modifying
    fun deleteByUserIdAndSongIdAndType(userId: UUID, songId: UUID, type: ReactionType)
}
