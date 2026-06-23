package com.newzic.domain.repository

import com.newzic.domain.entity.WorkspaceReferenceEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface WorkspaceReferenceRepository : JpaRepository<WorkspaceReferenceEntity, UUID> {

    fun findByWorkspaceIdOrderByCreatedAtDesc(workspaceId: UUID): List<WorkspaceReferenceEntity>

    fun countByWorkspaceId(workspaceId: UUID): Long
}
