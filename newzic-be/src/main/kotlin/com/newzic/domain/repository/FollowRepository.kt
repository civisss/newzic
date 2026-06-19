package com.newzic.domain.repository

import com.newzic.domain.entity.FollowEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface FollowRepository : JpaRepository<FollowEntity, UUID> {

    fun existsByFollowerIdAndFollowingId(followerId: UUID, followingId: UUID): Boolean

    fun findByFollowerIdAndFollowingId(followerId: UUID, followingId: UUID): FollowEntity?

    fun findByFollowerId(followerId: UUID): List<FollowEntity>

    fun findByFollowingId(followingId: UUID): List<FollowEntity>

    fun countByFollowingId(followingId: UUID): Long

    fun countByFollowerId(followerId: UUID): Long
}
