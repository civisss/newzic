package com.newzic.domain.repository

import com.newzic.domain.entity.WorkspaceFileEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface WorkspaceFileRepository : JpaRepository<WorkspaceFileEntity, UUID> {

    fun findByWorkspaceIdOrderByCreatedAtDesc(workspaceId: UUID): List<WorkspaceFileEntity>

    fun countByWorkspaceId(workspaceId: UUID): Long
}
