package com.newzic.domain.repository

import com.newzic.domain.entity.WorkspaceMemberEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface WorkspaceMemberRepository : JpaRepository<WorkspaceMemberEntity, UUID> {

    fun findByWorkspaceId(workspaceId: UUID): List<WorkspaceMemberEntity>

    fun findByWorkspaceIdAndUserId(workspaceId: UUID, userId: UUID): WorkspaceMemberEntity?

    fun existsByWorkspaceIdAndUserId(workspaceId: UUID, userId: UUID): Boolean

    fun countByWorkspaceId(workspaceId: UUID): Long
}
